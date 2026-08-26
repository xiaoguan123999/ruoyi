package com.ruoyi.biz.pay;

import java.util.Map;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizPayProvider;

/**
 * 模拟代收：不请求真实网关，返回本机收银台地址。验签规则与 MonPay 一致，方便以后无缝切换。
 */
@Component
public class MockPayAdapter implements IBizPayAdapter
{
    @Override
    public PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request)
    {
        PayCreateResult result = new PayCreateResult();
        result.setPayType("url");
        result.setProviderTradeNo("MOCK" + request.getOutTradeNo());
        String base = request.getBaseUrl();
        if (base == null || base.length() == 0)
        {
            base = "";
        }
        result.setPayUrl(base + "/pay/mock/cashier?outTradeNo=" + request.getOutTradeNo());
        return result;
    }

    @Override
    public boolean verifyNotify(BizPayProvider provider, Map<String, String> payload)
    {
        String secret = provider == null ? "" : provider.getSecretKey();
        String sign = payload == null ? null : payload.get("sign");
        return MonPaySign.verify(payload, secret, sign);
    }

    @Override
    public boolean isPaid(Map<String, String> payload)
    {
        if (payload == null)
        {
            return false;
        }
        String status = payload.get("trade_status");
        if (status == null)
        {
            status = payload.get("status");
        }
        return BizConstants.PAY_TRADE_SUCCESS.equals(status);
    }
}
