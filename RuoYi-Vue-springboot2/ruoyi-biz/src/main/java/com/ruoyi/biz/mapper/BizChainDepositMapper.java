package com.ruoyi.biz.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizChainDeposit;

@Mapper
public interface BizChainDepositMapper
{
    BizChainDeposit selectById(Long depositId);

    BizChainDeposit selectByOutTradeNo(String outTradeNo);

    BizChainDeposit selectByOutTradeNoForUpdate(String outTradeNo);

    BizChainDeposit selectPendingByPayAmountForUpdate(@Param("payAmount") BigDecimal payAmount,
            @Param("address") String address, @Param("network") String network);

    BizChainDeposit selectByTxHash(String txHash);

    int countPendingByPayAmount(@Param("payAmount") BigDecimal payAmount, @Param("address") String address,
            @Param("network") String network);

    Date selectMinPendingCreateTime();

    Date selectMinPendingCreateTimeByAddress(@Param("network") String network, @Param("address") String address);

    List<BizChainDeposit> selectPendingCollects();

    List<BizChainDeposit> selectPendingUnexpired();

    List<BizChainDeposit> selectExpiredPending();

    List<BizChainDeposit> selectList(BizChainDeposit query);

    int insertDeposit(BizChainDeposit row);

    int updateDeposit(BizChainDeposit row);
}
