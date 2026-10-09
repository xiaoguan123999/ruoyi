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
 * Jinmin (xing): form POST /api/pay/create_order and /api/pay/query_order.
 * Amount fen. Sign MD5 upper with &amp;key=. Notify reply success.
 * Paid status 2 or 3. channel.product_id = productId (8055 alipay / 8057 wechat).
 */
@Component
public class JinminAdapter implements IBizPayAdapter
{
    private static final Logger log = LoggerFactory.getLogger(JinminAdapter.class);

    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        String productId = nvl(request.getProductId(), "").trim();
        if (productId.length() == 0)
        {
            throw new ServiceException("拉单失败：未配置渠道编号");
        }
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mchId", nvl(provider.getAppId(), ""));
        params.put("productId", productId);
        params.put("mchOrderNo", request.getOutTradeNo());
        params.put("amount", PayHttp.fen(request.getAmount()));
        params.put("notifyUrl", request.getNotifyUrl());
        if (StringUtils.isNotEmpty(request.getReturnUrl()))
        {
            params.put("returnUrl", request.getReturnUrl());
        }
        params.put("sign", MonPaySign.signUpper(params, secret(provider)));
        log.info("jinmin createOrder outTradeNo={} productId={} amountFen={}",
                request.getOutTradeNo(), productId, params.get("amount"));
        String raw = PayHttp.postForm(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/pay/create_order"), encode(params));
        JSONObject json = parseObj(raw);
        if (!"SUCCESS".equalsIgnoreCase(json.getString("retCode")))
        {
            String gatewayMsg = nvl(json.getString("retMsg"), raw);
            log.warn("jinmin createOrder fail outTradeNo={} gatewayMsg={} raw={}",
                    request.getOutTradeNo(), gatewayMsg, cut(raw, 500));
            throw new ServiceException("拉单失败：" + gatewayMsg);
        }
        String payUrl = extractPayUrl(json);
        if (StringUtils.isEmpty(payUrl))
        {
            log.warn("jinmin createOrder empty payUrl outTradeNo={} raw={}", request.getOutTradeNo(), cut(raw, 500));
            throw new ServiceException("拉单失败：no pay url");
        }
        PayCreateResult result = new PayCreateResult();
        result.setPayUrl(payUrl);
        result.setPayType("url");
        result.setProviderTradeNo(json.getString("payOrderId"));
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
        String status = payload == null ? "" : nvl(payload.get("status"), "");
        return "2".equals(status) || "3".equals(status);
    }

    @Override
    public String notifySuccess()
    {
        return "success";
    }

    @Override
    public PayQueryResult queryOrder(BizPayProvider provider, String outTradeNo)
    {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mchId", nvl(provider.getAppId(), ""));
        params.put("mchOrderNo", outTradeNo);
        params.put("sign", MonPaySign.signUpper(params, secret(provider)));
        String raw = PayHttp.postForm(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/pay/query_order"), encode(params));
        JSONObject json = parseObj(raw);
        if (!"SUCCESS".equalsIgnoreCase(json.getString("retCode")))
        {
            log.warn("jinmin query fail outTradeNo={} raw={}", outTradeNo, cut(raw, 500));
            return PayQueryResult.unpaid(raw);
        }
        String status = json.getString("status");
        if ("2".equals(status) || "3".equals(status))
        {
            return PayQueryResult.paid(json.getString("payOrderId"), raw);
        }
        return PayQueryResult.unpaid(raw);
    }

    private static String extractPayUrl(JSONObject json)
    {
        JSONObject payParams = json.getJSONObject("payParams");
        if (payParams == null)
        {
            String raw = json.getString("payParams");
            if (StringUtils.isNotEmpty(raw) && raw.trim().startsWith("{"))
            {
                payParams = JSON.parseObject(raw);
            }
        }
        if (payParams == null)
        {
            return first(json, "payUrl", "pay_url", "payData");
        }
        return first(payParams, "payUrl", "pay_url", "payData", "codeImgUrl");
    }

    private static String encode(Map<String, String> params)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet())
        {
            if (sb.length() > 0)
            {
                sb.append('&');
            }
            try
            {
                sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8.name()));
                sb.append('=');
                sb.append(URLEncoder.encode(nvl(e.getValue(), ""), StandardCharsets.UTF_8.name()));
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

    private static String secret(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getSecretKey(), "");
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
