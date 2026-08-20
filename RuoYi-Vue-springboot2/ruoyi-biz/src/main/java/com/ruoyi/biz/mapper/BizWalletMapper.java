package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizWallet;

public interface BizWalletMapper
{
    BizWallet selectWallet(@Param("memberId") Long memberId, @Param("currency") String currency);

    BizWallet selectWalletForUpdate(@Param("memberId") Long memberId, @Param("currency") String currency);

    List<BizWallet> selectWalletsByMemberId(Long memberId);

    int insertWallet(BizWallet wallet);

    int updateWallet(BizWallet wallet);
}
