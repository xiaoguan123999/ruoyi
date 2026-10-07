package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizOrder;

public interface IBizOrderService
{
    BizOrder selectOrderById(Long orderId);

    List<BizOrder> selectOrderList(BizOrder order);

    /** 订单日返发放记录（按日期倒序） */
    java.util.List<com.ruoyi.biz.domain.BizRebateLog> selectRebateLogsByOrderId(Long orderId);

    BizOrder subscribe(Long memberId, Long productId, String currency, String payPassword, Integer quantity);

    int processDailyRebate();

    /** ASSIST 到期退本到 BALANCE */
    int processAssistPrincipalReturn();

    /** 已改为激活自动转入；接口保留，调用则提示无需手动结算 */
    BizOrder settleAccumulate(Long memberId, Long orderId);

    /** 同步快照后按新一拖二重算激活（已激活不撤销） */
    void refreshUnlockForSnapshot(Long memberId, Long productId);

    /**
     * 保护期延长纠偏：累计池超出「新保护期下应有金额」的部分转入产品收益。
     * @return 转入产品收益的金额，0 表示无需纠偏
     */
    java.math.BigDecimal correctProtectPoolAfterExtend(Long orderId, int oldProtectDays, int newProtectDays);
}
