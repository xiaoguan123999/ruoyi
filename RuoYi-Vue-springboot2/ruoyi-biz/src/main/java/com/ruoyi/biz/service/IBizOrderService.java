package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizOrder;

public interface IBizOrderService
{
    BizOrder selectOrderById(Long orderId);

    List<BizOrder> selectOrderList(BizOrder order);

    BizOrder subscribe(Long memberId, Long productId, String currency);

    int processDailyRebate();
}
