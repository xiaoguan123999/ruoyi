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

    int countWithdrawRequiredOrders(@Param("memberId") Long memberId, @Param("currency") String currency);

    int insertOrder(BizOrder order);

    int updateOrder(BizOrder order);
}
