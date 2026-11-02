package com.ruoyi.biz.pay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * Baile v2 (baile.6666666.life): form POST /api/newOrder /api/queryOrderV2.
 * Amount yuan. Sign MD5 lower. Notify status=ok, reply ok/success.
 * Query data.status paid/unpaid. channel.product_id = channelType.
 */
@Component
public class BaileV2Adapter implements IBizPayAdapter
{
    private static final Logger log = LoggerFactory.getLogger(BaileV2Adapter.class);

    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        String channelType = nvl(request.getProductId(), "").trim();
        if (channelType.length() == 0)
        {
            throw new ServiceException("拉单失败：未配置渠道编号");
        }
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("merchantId", mchId(provider));
        params.put("orderId", request.getOutTradeNo());
        params.put("orderAmount", PayHttp.yuan(request.getAmount()));
        params.put("channelType", channelType);
        params.put("notifyUrl", request.getNotifyUrl());
        if (StringUtils.isNotEmpty(request.getReturnUrl()))
        {
            params.put("returnUrl", request.getReturnUrl());
        }
        params.put("isForm", "2");
        params.put("order_title", "recharge");
        String payerIp = sanitizeIp(request.getClientIp());
        if (payerIp.length() > 0)
        {
            params.put("payer_ip", payerIp);
        }
        params.put("sign", MonPaySign.sign(params, secret(provider)));
        log.info("baile2 createOrder outTradeNo={} channelType={} amount={}",
                request.getOutTradeNo(), channelType, params.get("orderAmount"));
        String raw = PayHttp.postForm(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/newOrder"), encode(params));
        JSONObject json = parseObj(raw);
        if (!ok(json))
        {
            String gatewayMsg = nvl(first(json, "msg", "message"), raw);
            log.warn("baile2 createOrder fail outTradeNo={} gatewayMsg={} raw={}",
                    request.getOutTradeNo(), gatewayMsg, cut(raw, 500));
            throw new ServiceException("拉单失败：" + gatewayMsg);
        }
        JSONObject data = json.getJSONObject("data");
        String payUrl = extractPayUrl(data == null ? json : data);
        if (StringUtils.isEmpty(payUrl))
        {
            log.warn("baile2 createOrder empty payUrl outTradeNo={} raw={}", request.getOutTradeNo(), cut(raw, 500));
            throw new ServiceException("拉单失败：no pay url");
        }
        PayCreateResult result = new PayCreateResult();
        result.setPayUrl(payUrl);
        result.setPayType("url");
        result.setProviderTradeNo(first(data == null ? json : data, "systemOrder", "orderId", "payOrderId"));
        return result;
    }

    @Override
    public boolean verifyNotify(BizPayProvider provider, Map<String, String> payload)
    {
        String sign = payload == null ? null : payload.get("sign");
        return MonPaySign.sign(payload, secret(provider)).equalsIgnoreCase(nvl(sign, ""));
    }

    @Override
    public boolean isPaid(Map<String, String> payload)
    {
        if (payload == null)
        {
            return false;
        }
        String status = nvl(first(payload, "status"), "").trim().toLowerCase();
        return "ok".equals(status) || "paid".equals(status) || "success".equals(status);
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
        params.put("orderId", outTradeNo);
        params.put("sign", MonPaySign.sign(params, secret(provider)));
        String raw = PayHttp.postForm(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/queryOrderV2"), encode(params));
        JSONObject json = parseObj(raw);
        if (!ok(json))
        {
            log.warn("baile2 query fail outTradeNo={} raw={}", outTradeNo, cut(raw, 500));
            return PayQueryResult.unpaid(raw);
        }
        JSONObject data = json.getJSONObject("data");
        JSONObject src = data == null ? json : data;
        String status = nvl(first(src, "status"), "").trim().toLowerCase();
        if ("paid".equals(status) || "ok".equals(status) || "success".equals(status))
        {
            return PayQueryResult.paid(first(src, "systemOrder", "orderId"), raw);
        }
        return PayQueryResult.unpaid(raw);
    }

    private static boolean ok(JSONObject json)
    {
        Integer code = json.getInteger("code");
        return code != null && code.intValue() == 200;
    }

    private static String extractPayUrl(JSONObject obj)
    {
        return first(obj, "payUrl", "pay_url", "url");
    }

    private static String mchId(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getAppId(), "");
    }

    private static String secret(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getSecretKey(), "");
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

    private static String first(Map<String, String> map, String... keys)
    {
        if (map == null)
        {
            return "";
        }
        for (String key : keys)
        {
            String v = map.get(key);
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
