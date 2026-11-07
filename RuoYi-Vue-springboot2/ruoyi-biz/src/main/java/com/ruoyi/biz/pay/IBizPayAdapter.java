package com.ruoyi.biz.pay;

import java.util.Map;
import com.ruoyi.biz.domain.BizPayProvider;

public interface IBizPayAdapter
{
    PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request);

    boolean verifyNotify(BizPayProvider provider, Map<String, String> payload);

    boolean isPaid(Map<String, String> payload);

    /** 从回调参数取出我方商户单号 */
    default String extractOutTradeNo(Map<String, String> payload)
    {
        if (payload == null)
        {
            return "";
        }
        String[] keys = { "out_trade_no", "outTradeNo", "mchOrderNo", "mch_order_no" };
        for (int i = 0; i < keys.length; i++)
        {
            String v = payload.get(keys[i]);
            if (v != null && v.length() > 0)
            {
                return v;
            }
        }
        return "";
    }

    default String notifySuccess()
    {
        return "success";
    }

    default PayQueryResult queryOrder(BizPayProvider provider, String outTradeNo)
    {
        return null;
    }
}
