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

    /** ACCUMULATE 订单满周期结算累计进产品收益钱包 */
    BizOrder settleAccumulate(Long memberId, Long orderId);
}
