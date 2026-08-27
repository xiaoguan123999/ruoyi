package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.api.AppFundRecordItem;

public interface BizFundRecordMapper
{
    List<AppFundRecordItem> selectAppFundRecords(@Param("memberId") Long memberId,
            @Param("currency") String currency, @Param("status") String status,
            @Param("includeRecharge") boolean includeRecharge,
            @Param("includeWithdraw") boolean includeWithdraw);
}
