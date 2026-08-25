package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizOrder;

public interface BizOrderMapper
{
    BizOrder selectOrderById(Long orderId);

    List<BizOrder> selectOrderList(BizOrder order);

    List<BizOrder> selectHoldingOrders();

    int countMemberOrders(@Param("memberId") Long memberId);

    int countMemberProductOrders(@Param("memberId") Long memberId, @Param("productId") Long productId);

    int countWithdrawRequiredOrders(@Param("memberId") Long memberId, @Param("currency") String currency);

    java.math.BigDecimal sumTeamOrderAmount(@Param("memberId") Long memberId, @Param("currency") String currency,
            @Param("includeSelf") boolean includeSelf);

    int insertOrder(BizOrder order);

    int updateOrder(BizOrder order);
}
