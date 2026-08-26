package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizPayOrder;

public interface BizPayOrderMapper
{
    BizPayOrder selectPayOrderById(Long payOrderId);
    BizPayOrder selectPayOrderByOutTradeNo(String outTradeNo);
    BizPayOrder selectPayOrderByOutTradeNoForUpdate(String outTradeNo);
    List<BizPayOrder> selectPayOrderList(BizPayOrder query);
    int insertPayOrder(BizPayOrder row);
    int updatePayOrder(BizPayOrder row);
}
