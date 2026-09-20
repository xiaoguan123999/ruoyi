package com.ruoyi.biz.pay;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * MonPay / NicePay family: FeiFan, Baifu, Baoli.
 * POST JSON /api/order, amount in yuan, MD5 lower sign.
 */
@Component
public class MonPayAdapter implements IBizPayAdapter
{
    private static final Logger log = LoggerFactory.getLogger(MonPayAdapter.class);

    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        Map<String, String> body = signed(provider, request);
        log.info("monpay createOrder outTradeNo={} productId={} amount={}",
                request.getOutTradeNo(), request.getProductId(), request.getAmount());
        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/order"), JSON.toJSONString(body));
        JSONObject json = parseObj(raw);
        if (json.getIntValue("code") != 200)
        {
            String gatewayMsg = first(json, "message", "msg");
            log.warn("monpay createOrder fail outTradeNo={} gatewayMsg={} raw={}",
                    request.getOutTradeNo(), gatewayMsg, cut(raw, 500));
            throw new ServiceException("拉单失败：" + gatewayMsg);
        }
        JSONObject data = json.getJSONObject("data");
        if (data == null)
        {
            log.warn("monpay createOrder empty data outTradeNo={} raw={}", request.getOutTradeNo(), cut(raw, 500));
            throw new ServiceException("拉单失败：empty data");
        }
        String url = first(data, "url", "payUrl", "pay_url");
        if (StringUtils.isEmpty(url))
        {
            log.warn("monpay createOrder empty payUrl outTradeNo={} raw={}", request.getOutTradeNo(), cut(raw, 500));
            throw new ServiceException("拉单失败：no pay url");
        }
        PayCreateResult result = new PayCreateResult();
        result.setPayUrl(url);
        result.setPayType(nvl(first(data, "type", "payType"), "url"));
        result.setProviderTradeNo(first(data, "trade_no", "tradeNo"));
        return result;
    }

    @Override
    public boolean verifyNotify(BizPayProvider provider, Map<String, String> payload)
    {
        String sign = payload == null ? null : payload.get("sign");
        return MonPaySign.verify(payload, secret(provider), sign);
    }

    @Override
    public boolean isPaid(Map<String, String> payload)
    {
        if (payload == null)
        {
            return false;
        }
        String status = first(payload, "trade_status", "tradeStatus", "status");
        return BizConstants.PAY_TRADE_SUCCESS.equals(status);
    }

    @Override
    public PayQueryResult queryOrder(BizPayProvider provider, String outTradeNo)
    {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("out_trade_no", outTradeNo);
        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/order/status"),
                JSON.toJSONString(signedBase(provider, params)));
        JSONObject json = parseObj(raw);
        if (json.getIntValue("code") != 200)
        {
            return PayQueryResult.unpaid(raw);
        }
        JSONObject data = json.getJSONObject("data");
        if (data == null)
        {
            return PayQueryResult.unpaid(raw);
        }
        String status = first(data, "trade_status", "tradeStatus", "status");
        if (BizConstants.PAY_TRADE_SUCCESS.equals(status))
        {
            return PayQueryResult.paid(first(data, "trade_no", "tradeNo"), raw);
        }
        return PayQueryResult.unpaid(raw);
    }

    private static Map<String, String> signed(BizPayProvider provider, PayCreateRequest request)
    {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("product_id", nvl(request.getProductId(), ""));
        params.put("amount", PayHttp.yuan(request.getAmount()));
        params.put("out_trade_no", request.getOutTradeNo());
        params.put("notify_url", request.getNotifyUrl());
        return signedBase(provider, params);
    }

    private static Map<String, String> signedBase(BizPayProvider provider, Map<String, String> extra)
    {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("app_id", nvl(provider.getAppId(), ""));
        params.put("time", String.valueOf(System.currentTimeMillis() / 1000L));
        if (extra != null)
        {
            params.putAll(extra);
        }
        params.put("sign", MonPaySign.sign(params, secret(provider)));
        return params;
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

    private static String secret(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getSecretKey(), "");
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
