package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.ruoyi.biz.domain.BizPayOrder;

@Mapper
public interface BizPayOrderMapper
{
    BizPayOrder selectPayOrderById(Long payOrderId);
    BizPayOrder selectPayOrderByOutTradeNo(String outTradeNo);
    BizPayOrder selectPayOrderByOutTradeNoForUpdate(String outTradeNo);
    List<BizPayOrder> selectPayOrderList(BizPayOrder query);
    int insertPayOrder(BizPayOrder row);
    int updatePayOrder(BizPayOrder row);

    /** 已过期仍待付的商户单号（定时关单） */
    List<String> selectExpiredWaitOutTradeNos();
}
