package com.ruoyi.biz.pay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * Arctic Ocean (北冰洋): form POST /pay/newOrder /pay/query.
 * Amount yuan 2 decimals. Sign MD5 upper. Notify reply ok. orderStatus 1=paid.
 * channel.product_id = service (from merchant ops).
 */
@Component
public class ArcticOceanAdapter implements IBizPayAdapter
{
    private static final Logger log = LoggerFactory.getLogger(ArcticOceanAdapter.class);

    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        String service = nvl(request.getProductId(), "").trim();
        if (service.length() == 0)
        {
            throw new ServiceException("拉单失败：未配置渠道编号");
        }
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("service", service);
        params.put("merchantId", mchId(provider));
        params.put("outTradeNo", request.getOutTradeNo());
        params.put("goodsDesc", "余额充值");
        params.put("amount", PayHttp.yuan(request.getAmount()));
        String clientIp = sanitizeIp(request.getClientIp());
        if (clientIp.length() > 0)
        {
            params.put("clientIp", clientIp);
        }
        params.put("notifyUrl", request.getNotifyUrl());
        if (StringUtils.isNotEmpty(request.getReturnUrl()))
        {
            params.put("returnUrl", request.getReturnUrl());
        }
        params.put("nonceStr", nonce());
        params.put("sign", MonPaySign.signUpper(params, secret(provider)));
        log.info("arctic createOrder outTradeNo={} service={} amount={}",
                request.getOutTradeNo(), service, params.get("amount"));
        String raw = PayHttp.postForm(PayHttp.joinUrl(provider.getGatewayUrl(), "/pay/newOrder"), encode(params));
        JSONObject json = parseObj(raw);
        if (!"0".equals(nvl(json.getString("code"), "")))
        {
            String gatewayMsg = nvl(first(json, "msg", "message"), raw);
            log.warn("arctic createOrder fail outTradeNo={} gatewayMsg={} raw={}",
                    request.getOutTradeNo(), gatewayMsg, cut(raw, 500));
            throw new ServiceException("拉单失败：" + gatewayMsg);
        }
        String payUrl = first(json, "result", "payUrl", "pay_url");
        if (StringUtils.isEmpty(payUrl))
        {
            log.warn("arctic createOrder empty payUrl outTradeNo={} raw={}", request.getOutTradeNo(), cut(raw, 500));
            throw new ServiceException("拉单失败：no pay url");
        }
        PayCreateResult result = new PayCreateResult();
        result.setPayUrl(payUrl);
        result.setPayType("url");
        result.setProviderTradeNo(first(json, "orderCode", "order_code"));
        return result;
    }

    @Override
    public boolean verifyNotify(BizPayProvider provider, Map<String, String> payload)
    {
        String sign = payload == null ? null : payload.get("sign");
        return MonPaySign.signUpper(payload, secret(provider)).equalsIgnoreCase(nvl(sign, ""));
    }

    @Override
    public boolean isPaid(Map<String, String> payload)
    {
        if (payload == null)
        {
            return false;
        }
        String code = nvl(payload.get("code"), "0");
        if (code.length() > 0 && !"0".equals(code))
        {
            return false;
        }
        return "1".equals(nvl(payload.get("orderStatus"), ""));
    }

    @Override
    public String notifySuccess()
    {
        return "ok";
    }

    @Override
    public PayQueryResult queryOrder(BizPayProvider provider, String outTradeNo)
    {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("merchantId", mchId(provider));
        params.put("outTradeNo", outTradeNo);
        params.put("nonceStr", nonce());
        params.put("sign", MonPaySign.signUpper(params, secret(provider)));
        String raw = PayHttp.postForm(PayHttp.joinUrl(provider.getGatewayUrl(), "/pay/query"), encode(params));
        JSONObject json = parseObj(raw);
        if (!"0".equals(nvl(json.getString("code"), "")))
        {
            return PayQueryResult.unpaid(raw);
        }
        if ("1".equals(nvl(json.getString("orderStatus"), "")))
        {
            return PayQueryResult.paid(first(json, "orderCode", "order_code"), raw);
        }
        return PayQueryResult.unpaid(raw);
    }

    private static String mchId(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getAppId(), "");
    }

    private static String secret(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getSecretKey(), "");
    }

    private static String nonce()
    {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private static String sanitizeIp(String ip)
    {
        String v = nvl(ip, "").trim();
        if (v.length() == 0 || "127.0.0.1".equals(v) || "https://example.org/m/orchid".equals(v)
                || "0:0:0:0:0:0:0:1".equals(v) || "::1".equals(v))
        {
            return "";
        }
        return v;
    }

    private static String encode(Map<String, String> params)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet())
        {
            if (e.getValue() == null || e.getValue().length() == 0)
            {
                continue;
            }
            if (sb.length() > 0)
            {
                sb.append('&');
            }
            try
            {
                sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8.name()));
                sb.append('=');
                sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8.name()));
            }
            catch (Exception ex)
            {
                throw new ServiceException("encode fail");
            }
        }
        return sb.toString();
    }

    private static JSONObject parseObj(String raw)
    {
        JSONObject json = JSON.parseObject(raw);
        if (json == null)
        {
            throw new ServiceException("支付网关返回无效");
        }
        return json;
    }

    private static String first(JSONObject obj, String... keys)
    {
        if (obj == null)
        {
            return "";
        }
        for (String key : keys)
        {
            String v = obj.getString(key);
            if (StringUtils.isNotEmpty(v))
            {
                return v;
            }
        }
        return "";
    }

    private static String nvl(String v, String fallback)
    {
        return StringUtils.isEmpty(v) ? fallback : v;
    }

    private static String cut(String raw, int max)
    {
        if (raw == null)
        {
            return "";
        }
        return raw.length() <= max ? raw : raw.substring(0, max);
    }
}
