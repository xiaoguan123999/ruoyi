package com.ruoyi.web.controller.pay;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.biz.service.IBizOnlinePayService;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "支付回调")
@RestController
@RequestMapping("/pay")
public class PayCallbackController
{
    @Autowired
    private IBizOnlinePayService onlinePayService;

    @ApiOperation("三方代收异步通知")
    @PostMapping(value = "/callback/{providerCode}/deposit", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyPost(@PathVariable String providerCode, HttpServletRequest request,
            @RequestBody(required = false) String raw)
    {
        Map<String, String> payload = parsePayload(request, raw);
        return onlinePayService.handleNotify(providerCode, payload, StringUtils.isEmpty(raw) ? JSON.toJSONString(payload) : raw);
    }

    @ApiOperation("三方代收异步通知 GET")
    @GetMapping(value = "/callback/{providerCode}/deposit", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyGet(@PathVariable String providerCode, HttpServletRequest request)
    {
        Map<String, String> payload = queryMap(request);
        return onlinePayService.handleNotify(providerCode, payload, JSON.toJSONString(payload));
    }

    @ApiOperation("模拟收银台")
    @GetMapping(value = "/mock/cashier", produces = MediaType.TEXT_HTML_VALUE)
    public String cashier(@RequestParam("outTradeNo") String outTradeNo)
    {
        return onlinePayService.mockCashierHtml(outTradeNo);
    }

    @ApiOperation("模拟收银台确认支付")
    @PostMapping(value = "/mock/pay", produces = MediaType.TEXT_HTML_VALUE)
    public String mockPay(@RequestParam("outTradeNo") String outTradeNo)
    {
        onlinePayService.simulatePaid(outTradeNo, "mock-cashier");
        return onlinePayService.mockCashierHtml(outTradeNo);
    }

    private static Map<String, String> parsePayload(HttpServletRequest request, String raw)
    {
        Map<String, String> payload = queryMap(request);
        if (StringUtils.isEmpty(raw))
        {
            return payload;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("{"))
        {
            JSONObject obj = JSON.parseObject(trimmed);
            if (obj != null)
            {
                JSONObject nested = obj.getJSONObject("data");
                if (nested == null)
                {
                    nested = obj.getJSONObject("result");
                }
                JSONObject src = nested != null ? nested : obj;
                for (String key : src.keySet())
                {
                    Object val = src.get(key);
                    if (val != null)
                    {
                        payload.put(key, String.valueOf(val));
                    }
                }
            }
            return payload;
        }
        for (String pair : trimmed.split("&"))
        {
            int idx = pair.indexOf('=');
            if (idx > 0)
            {
                payload.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return payload;
    }

    private static Map<String, String> queryMap(HttpServletRequest request)
    {
        Map<String, String> payload = new LinkedHashMap<String, String>();
        Map<String, String[]> params = request.getParameterMap();
        if (params != null)
        {
            for (Map.Entry<String, String[]> e : params.entrySet())
            {
                if (e.getValue() != null && e.getValue().length > 0 && e.getValue()[0] != null)
                {
                    payload.put(e.getKey(), e.getValue()[0]);
                }
            }
        }
        return payload;
    }
}
