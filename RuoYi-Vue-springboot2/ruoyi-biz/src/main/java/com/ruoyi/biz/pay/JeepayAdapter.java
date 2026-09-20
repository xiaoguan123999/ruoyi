package com.ruoyi.biz.pay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * FuWang / Jeepay: JSON POST /api/pay/unifiedorder, amount in fen, MD5 upper.
 * app_id may be mchNo or mchNo|appId.
 */
@Component
public class JeepayAdapter implements IBizPayAdapter
{
    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        String[] ids = splitMch(provider.getAppId());
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mchNo", ids[0]);
        params.put("appId", ids[1]);
        params.put("mchOrderNo", request.getOutTradeNo());
        params.put("wayCode", nvl(request.getProductId(), ""));
        params.put("amount", PayHttp.fen(request.getAmount()));
        params.put("currency", "CNY");
        params.put("clientIp", nvl(request.getClientIp(), "127.0.0.1"));
        params.put("subject", "\u4f59\u989d\u5145\u503c");
        params.put("body", "\u4f59\u989d\u5145\u503c");
        params.put("notifyUrl", request.getNotifyUrl());
        if (StringUtils.isNotEmpty(request.getReturnUrl()))
        {
            params.put("returnUrl", request.getReturnUrl());
        }
        params.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        params.put("version", "1.0");
        params.put("signType", "MD5");
        params.put("sign", MonPaySign.signUpper(params, secret(provider)));
        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/pay/unifiedorder"),
                JSON.toJSONString(params));
        JSONObject json = parseObj(raw);
        JSONObject data = json.getJSONObject("data");
        if (!ok(json))
        {
            throw new ServiceException("\u62c9\u5355\u5931\u8d25\uff1a" + first(json, "msg", "message", "retMsg"));
        }
        String payUrl = extractPayUrl(data == null ? json : data);
        if (StringUtils.isEmpty(payUrl))
        {
            throw new ServiceException("\u62c9\u5355\u5931\u8d25\uff1ano pay url");
        }
        PayCreateResult result = new PayCreateResult();
        result.setPayUrl(payUrl);
        result.setPayType(nvl(first(data == null ? json : data, "payDataType", "payType"), "url"));
        result.setProviderTradeNo(first(data == null ? json : data, "payOrderId", "pay_order_id"));
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
        String state = first(payload, "state", "orderState", "status");
        return "2".equals(state);
    }

    @Override
    public PayQueryResult queryOrder(BizPayProvider provider, String outTradeNo)
    {
        String[] ids = splitMch(provider.getAppId());
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("mchNo", ids[0]);
        params.put("appId", ids[1]);
        params.put("mchOrderNo", outTradeNo);
        params.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        params.put("version", "1.0");
        params.put("signType", "MD5");
        params.put("sign", MonPaySign.signUpper(params, secret(provider)));
        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/pay/query"),
                JSON.toJSONString(params));
        JSONObject json = parseObj(raw);
        if (!ok(json))
        {
            return PayQueryResult.unpaid(raw);
        }
        JSONObject data = json.getJSONObject("data");
        JSONObject src = data == null ? json : data;
        String state = first(src, "state", "orderState", "status");
        if ("2".equals(state))
        {
            return PayQueryResult.paid(first(src, "payOrderId", "pay_order_id"), raw);
        }
        return PayQueryResult.unpaid(raw);
    }

    private static boolean ok(JSONObject json)
    {
        if ("SUCCESS".equalsIgnoreCase(json.getString("retCode")))
        {
            return true;
        }
        Integer code = json.getInteger("code");
        return code != null && code.intValue() == 0;
    }

    private static String extractPayUrl(JSONObject obj)
    {
        String url = first(obj, "payData", "payUrl", "pay_url", "payOrderId");
        if (StringUtils.isNotEmpty(url) && (url.startsWith("http://") || url.startsWith("https://")))
        {
            return url;
        }
        String payData = obj.getString("payData");
        if (StringUtils.isNotEmpty(payData) && payData.trim().startsWith("{"))
        {
            JSONObject nested = JSON.parseObject(payData);
            return first(nested, "payUrl", "url", "codeUrl");
        }
        return url != null && url.startsWith("http") ? url : first(obj, "payUrl", "url", "codeUrl");
    }

    private static String[] splitMch(String appId)
    {
        String raw = nvl(appId, "");
        int idx = raw.indexOf('|');
        if (idx > 0 && idx < raw.length() - 1)
        {
            return new String[] { raw.substring(0, idx), raw.substring(idx + 1) };
        }
        return new String[] { raw, raw };
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

    private static String secret(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getSecretKey(), "");
    }

    private static String nvl(String v, String fallback)
    {
        return StringUtils.isEmpty(v) ? fallback : v;
    }
}
