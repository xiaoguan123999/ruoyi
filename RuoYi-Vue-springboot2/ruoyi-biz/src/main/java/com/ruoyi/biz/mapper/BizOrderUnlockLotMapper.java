package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizOrderUnlockLot;

public interface BizOrderUnlockLotMapper
{
    List<BizOrderUnlockLot> selectByOrderId(@Param("orderId") Long orderId);

    int insertLot(BizOrderUnlockLot lot);

    int updateLot(BizOrderUnlockLot lot);
}
