package com.ruoyi.biz.pay;

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
 * FuWang API 文档 v1（https://fuwang-mch-idllb.aaagood.xyz/doc ）。
 * <ul>
 *   <li>POST JSON {@code /api/pay/unifiedorder}</li>
 *   <li>字段：mchId / wayCode(int) / outTradeNo / amount(分) / subject / clientIp / notifyUrl / reqTime / sign</li>
 *   <li>签名：非空参数字典序拼接后 MD5 小写，末尾 {@code &key=商户密钥}</li>
 *   <li>回调成功应答：SUCCESS 或 OK；订单 state：1=支付成功</li>
 *   <li>channel.product_id = 数字 wayCode（商户后台通道类型，文档示例 901）</li>
 *   <li>provider.app_id = 商户号 mchId</li>
 * </ul>
 */
@Component
public class JeepayAdapter implements IBizPayAdapter
{
    private static final Logger log = LoggerFactory.getLogger(JeepayAdapter.class);

    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        String mchId = mchId(provider);
        long amountFen = Long.parseLong(PayHttp.fen(request.getAmount()));
        long wayCode = parseWayCode(request.getProductId());
        long reqTime = System.currentTimeMillis();
        String clientIp = nvl(request.getClientIp(), "127.0.0.1");
        if ("127.0.0.1".equals(clientIp) || "https://example.org/m/orchid".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp))
        {
            // 文档要求尽量填写真实客户端 IPv4；本地环回时用公网占位避免网关拒单
            clientIp = "1.1.1.1";
        }

        Map<String, String> signParams = new LinkedHashMap<String, String>();
        signParams.put("mchId", mchId);
        signParams.put("wayCode", String.valueOf(wayCode));
        signParams.put("subject", "余额充值");
        signParams.put("body", "余额充值");
        signParams.put("outTradeNo", request.getOutTradeNo());
        signParams.put("amount", String.valueOf(amountFen));
        signParams.put("clientIp", clientIp);
        signParams.put("notifyUrl", request.getNotifyUrl());
        if (StringUtils.isNotEmpty(request.getReturnUrl()))
        {
            signParams.put("returnUrl", request.getReturnUrl());
        }
        signParams.put("reqTime", String.valueOf(reqTime));
        // 文档：MD5 32 位小写
        String sign = MonPaySign.sign(signParams, secret(provider));

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("mchId", mchId);
        body.put("wayCode", Long.valueOf(wayCode));
        body.put("subject", "余额充值");
        body.put("body", "余额充值");
        body.put("outTradeNo", request.getOutTradeNo());
        body.put("amount", Long.valueOf(amountFen));
        body.put("clientIp", clientIp);
        body.put("notifyUrl", request.getNotifyUrl());
        if (StringUtils.isNotEmpty(request.getReturnUrl()))
        {
            body.put("returnUrl", request.getReturnUrl());
        }
        body.put("reqTime", Long.valueOf(reqTime));
        body.put("sign", sign);

        String payload = JSON.toJSONString(body);
        log.info("fuwang createOrder outTradeNo={} wayCode={} amountFen={} body={}",
                request.getOutTradeNo(), Long.valueOf(wayCode), Long.valueOf(amountFen), payload);
        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/pay/unifiedorder"), payload);
        JSONObject json = parseObj(raw);
        if (!ok(json))
        {
            String gatewayMsg = first(json, "msg", "message", "retMsg");
            log.warn("fuwang createOrder fail outTradeNo={} gatewayMsg={} raw={}",
                    request.getOutTradeNo(), gatewayMsg, cut(raw, 500));
            throw new ServiceException("拉单失败：" + gatewayMsg);
        }
        JSONObject data = json.getJSONObject("data");
        String payUrl = extractPayUrl(data == null ? json : data);
        if (StringUtils.isEmpty(payUrl))
        {
            log.warn("fuwang createOrder empty payUrl outTradeNo={} raw={}", request.getOutTradeNo(), cut(raw, 500));
            throw new ServiceException("拉单失败：no pay url");
        }
        PayCreateResult result = new PayCreateResult();
        result.setPayUrl(payUrl);
        result.setPayType("url");
        result.setProviderTradeNo(first(data == null ? json : data, "tradeNo", "payOrderId", "pay_order_id"));
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
        // 文档：0待支付 1支付成功 2支付失败 3未出码 4异常
        String state = first(payload, "state", "orderState", "status");
        return "1".equals(state);
    }

    @Override
    public String notifySuccess()
    {
        return "SUCCESS";
    }

    @Override
    public PayQueryResult queryOrder(BizPayProvider provider, String outTradeNo)
    {
        String mchId = mchId(provider);
        long reqTime = System.currentTimeMillis();
        Map<String, String> signParams = new LinkedHashMap<String, String>();
        signParams.put("mchId", mchId);
        signParams.put("outTradeNo", outTradeNo);
        signParams.put("reqTime", String.valueOf(reqTime));
        String sign = MonPaySign.sign(signParams, secret(provider));

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("mchId", mchId);
        body.put("outTradeNo", outTradeNo);
        body.put("reqTime", Long.valueOf(reqTime));
        body.put("sign", sign);

        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/pay/query"),
                JSON.toJSONString(body));
        JSONObject json = parseObj(raw);
        if (!ok(json))
        {
            log.warn("fuwang query fail outTradeNo={} raw={}", outTradeNo, cut(raw, 500));
            return PayQueryResult.unpaid(raw);
        }
        JSONObject data = json.getJSONObject("data");
        JSONObject src = data == null ? json : data;
        String state = first(src, "state", "orderState", "status");
        if ("1".equals(state))
        {
            return PayQueryResult.paid(first(src, "tradeNo", "payOrderId", "pay_order_id"), raw);
        }
        return PayQueryResult.unpaid(raw);
    }

    private static long parseWayCode(String productId)
    {
        if (StringUtils.isEmpty(productId))
        {
            throw new ServiceException("拉单失败：未配置支付产品编码(wayCode)");
        }
        String raw = productId.trim();
        try
        {
            return Long.parseLong(raw);
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("拉单失败：福旺 wayCode 须为数字，当前=" + raw);
        }
    }

    private static String mchId(BizPayProvider provider)
    {
        String raw = provider == null ? "" : nvl(provider.getAppId(), "");
        int idx = raw.indexOf('|');
        if (idx > 0)
        {
            return raw.substring(0, idx);
        }
        return raw;
    }

    private static boolean ok(JSONObject json)
    {
        Integer code = json.getInteger("code");
        return code != null && code.intValue() == 0;
    }

    private static String extractPayUrl(JSONObject obj)
    {
        String url = first(obj, "payUrl", "payData", "pay_url");
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
        return url != null && url.startsWith("http") ? url : first(obj, "url", "codeUrl");
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
