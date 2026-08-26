package com.ruoyi.biz.pay;

import java.util.Map;
import com.ruoyi.biz.domain.BizPayProvider;

public interface IBizPayAdapter
{
    PayCreateResult createOrder(BizPayProvider provider, PayCreateRequest request);

    boolean verifyNotify(BizPayProvider provider, Map<String, String> payload);

    boolean isPaid(Map<String, String> payload);
}
