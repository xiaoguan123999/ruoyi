package com.ruoyi.biz.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizRebateLog;

public interface BizRebateLogMapper
{
    int insertRebateLog(BizRebateLog log);

    BigDecimal sumAmountByMemberAndCurrency(@Param("memberId") Long memberId, @Param("currency") String currency);
}
