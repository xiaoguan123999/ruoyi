package com.ruoyi.biz.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizRecharge;

public interface BizRechargeMapper
{
    BizRecharge selectRechargeById(Long rechargeId);

    List<BizRecharge> selectRechargeList(BizRecharge recharge);

    BigDecimal sumPassedRecharge(@Param("memberId") Long memberId, @Param("currency") String currency);

    BigDecimal sumTeamPassedRecharge(@Param("memberId") Long memberId, @Param("currency") String currency,
            @Param("includeSelf") boolean includeSelf);

    int insertRecharge(BizRecharge recharge);

    int updateRecharge(BizRecharge recharge);
}
