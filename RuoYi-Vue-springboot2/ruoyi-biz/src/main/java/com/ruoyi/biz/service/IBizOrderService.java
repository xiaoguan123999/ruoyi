package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizOrder;

public interface IBizOrderService
{
    BizOrder selectOrderById(Long orderId);

    List<BizOrder> selectOrderList(BizOrder order);

    BizOrder subscribe(Long memberId, Long productId, String currency, String payPassword, Integer quantity);

    int processDailyRebate();

    /** ASSIST 到期退本到 BALANCE */
    int processAssistPrincipalReturn();

    /** 已改为激活自动转入；接口保留，调用则提示无需手动结算 */
    BizOrder settleAccumulate(Long memberId, Long orderId);
}
