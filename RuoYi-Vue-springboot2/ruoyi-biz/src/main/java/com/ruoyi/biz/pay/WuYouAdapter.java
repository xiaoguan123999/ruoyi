package com.ruoyi.biz.pay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * WuYou / xxpay: form POST, amount in fen, MD5 upper sign.
 */
@Component
public class WuYouAdapter implements IBizPayAdapter
{
    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mchId", nvl(provider.getAppId(), ""));
        params.put("productId", nvl(request.getProductId(), ""));
        params.put("mchOrderNo", request.getOutTradeNo());
        params.put("amount", PayHttp.fen(request.getAmount()));
        params.put("notifyUrl", request.getNotifyUrl());
        if (StringUtils.isNotEmpty(request.getReturnUrl()))
        {
            params.put("returnUrl", request.getReturnUrl());
        }
        params.put("sign", MonPaySign.signUpper(params, secret(provider)));
        String raw = PayHttp.postForm(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/pay/create_order"), encode(params));
        JSONObject json = parseObj(raw);
        if (!"SUCCESS".equalsIgnoreCase(json.getString("retCode")))
        {
            throw new ServiceException("\u62c9\u5355\u5931\u8d25\uff1a" + nvl(json.getString("retMsg"), raw));
        }
        String payUrl = extractPayUrl(json);
        if (StringUtils.isEmpty(payUrl))
        {
            throw new ServiceException("\u62c9\u5355\u5931\u8d25\uff1ano pay url");
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
            throw new ServiceException("\u652f\u4ed8\u7f51\u5173\u8fd4\u56de\u65e0\u6548");
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
}
