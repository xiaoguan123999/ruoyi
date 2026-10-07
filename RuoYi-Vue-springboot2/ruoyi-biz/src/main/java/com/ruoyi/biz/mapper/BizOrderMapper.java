package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizOrder;

public interface BizOrderMapper
{
    BizOrder selectOrderById(Long orderId);

    List<BizOrder> selectOrderList(BizOrder order);

    List<BizOrder> selectHoldingOrders();

    /** ASSIST 到期待退本 */
    List<BizOrder> selectAssistDueReturnOrders();

    int countMemberOrders(@Param("memberId") Long memberId);

    int countMemberProductOrders(@Param("memberId") Long memberId, @Param("productId") Long productId);

    /** 会员某产品已激活份数（unlock lot 有 activate_time） */
    int sumActivatedLotsByMemberProduct(@Param("memberId") Long memberId, @Param("productId") Long productId);

    /** 会员对某对档产品已消耗的结算份额（各累计订单 related_slots_used 之和） */
    int sumRelatedSlotsUsedByMemberProduct(@Param("memberId") Long memberId, @Param("relatedProductId") Long relatedProductId);

    List<BizOrder> selectDirectDownlineProductOrders(@Param("parentId") Long parentId, @Param("productId") Long productId);

    List<BizOrder> selectDirectDownlineOrders(@Param("parentId") Long parentId);

    int countWithdrawRequiredOrders(@Param("memberId") Long memberId, @Param("currency") String currency);

    java.math.BigDecimal sumTeamOrderAmount(@Param("memberId") Long memberId, @Param("currency") String currency,
            @Param("includeSelf") boolean includeSelf, @Param("maxDepth") Integer maxDepth,
            @Param("viewerDepth") Integer viewerDepth);

    int insertOrder(BizOrder order);

    int updateOrder(BizOrder order);

    int clearOrderIncomeStart(@Param("orderId") Long orderId);

    /** 同步日返产品规则快照，不改剩余天数/累计现场/激活时间 */
    int updateOrderSnapshot(BizOrder order);

    /** 同步助力产品有限快照：提现指定/本金返还/助力值展示；不改价格 */
    int updateAssistOrderSnapshot(BizOrder order);
}
