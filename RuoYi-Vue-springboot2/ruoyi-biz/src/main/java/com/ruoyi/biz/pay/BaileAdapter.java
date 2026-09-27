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
 * 百乐支付（文档 https://fuckbaile.jkosiuwn.xyz/api/doc ）。
 * <ul>
 *   <li>POST JSON {@code /api/createorder}、{@code /api/queryorder}</li>
 *   <li>字段：mchId / wayCode(string) / outTradeNo / amount(分) / subject / clientIp / notifyUrl / reqTime / sign</li>
 *   <li>签名：非空参数字典序拼接后 MD5 小写，末尾 {@code &key=商户密钥}</li>
 *   <li>回调成功应答：SUCCESS 或 OK；订单 state：2=支付成功</li>
 *   <li>channel.product_id = wayCode（通道编码，向客服索取）</li>
 *   <li>provider.app_id = 商户号 mchId</li>
 * </ul>
 */
@Component
public class BaileAdapter implements IBizPayAdapter
{
    private static final Logger log = LoggerFactory.getLogger(BaileAdapter.class);

    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        String mchId = mchId(provider);
        long amountFen = Long.parseLong(PayHttp.fen(request.getAmount()));
        String wayCode = wayCode(request.getProductId());
        long reqTime = System.currentTimeMillis();
        String clientIp = nvl(request.getClientIp(), "127.0.0.1");
        if ("127.0.0.1".equals(clientIp) || "https://example.org/m/orchid".equals(clientIp)
                || "0:0:0:0:0:0:0:1".equals(clientIp))
        {
            clientIp = "1.1.1.1";
        }

        Map<String, String> signParams = new LinkedHashMap<String, String>();
        signParams.put("mchId", mchId);
        signParams.put("wayCode", wayCode);
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
        String sign = MonPaySign.sign(signParams, secret(provider));

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("mchId", mchId);
        body.put("wayCode", wayCode);
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
        log.info("baile createOrder outTradeNo={} wayCode={} amountFen={} body={}",
                request.getOutTradeNo(), wayCode, Long.valueOf(amountFen), payload);
        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/createorder"), payload);
        JSONObject json = parseObj(raw);
        if (!ok(json))
        {
            String gatewayMsg = first(json, "msg", "message", "retMsg");
            log.warn("baile createOrder fail outTradeNo={} gatewayMsg={} raw={}",
                    request.getOutTradeNo(), gatewayMsg, cut(raw, 500));
            throw new ServiceException("拉单失败：" + gatewayMsg);
        }
        JSONObject data = json.getJSONObject("data");
        String payUrl = extractPayUrl(data == null ? json : data);
        if (StringUtils.isEmpty(payUrl))
        {
            log.warn("baile createOrder empty payUrl outTradeNo={} raw={}", request.getOutTradeNo(), cut(raw, 500));
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
        // 文档：0未出码 1待支付 2支付成功 3支付失败 4冲正
        String state = first(payload, "state", "orderState", "status");
        return "2".equals(state);
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

        String raw = PayHttp.postJson(PayHttp.joinUrl(provider.getGatewayUrl(), "/api/queryorder"),
                JSON.toJSONString(body));
        JSONObject json = parseObj(raw);
        if (!ok(json))
        {
            log.warn("baile query fail outTradeNo={} raw={}", outTradeNo, cut(raw, 500));
            return PayQueryResult.unpaid(raw);
        }
        JSONObject data = json.getJSONObject("data");
        JSONObject src = data == null ? json : data;
        String state = first(src, "state", "orderState", "status");
        if ("2".equals(state))
        {
            return PayQueryResult.paid(first(src, "tradeNo", "payOrderId", "pay_order_id"), raw);
        }
        return PayQueryResult.unpaid(raw);
    }

    private static String wayCode(String productId)
    {
        if (StringUtils.isEmpty(productId))
        {
            throw new ServiceException("拉单失败：未配置支付产品编码(wayCode)");
        }
        return productId.trim();
    }

    private static String mchId(BizPayProvider provider)
    {
        return provider == null ? "" : nvl(provider.getAppId(), "");
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
