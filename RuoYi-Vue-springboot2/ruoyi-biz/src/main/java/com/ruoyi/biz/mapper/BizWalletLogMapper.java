package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizWalletLog;

public interface BizWalletLogMapper
{
    List<BizWalletLog> selectWalletLogList(BizWalletLog log);

    List<BizWalletLog> selectAppWalletLogList(@Param("memberId") Long memberId, @Param("currency") String currency,
            @Param("bizType") String bizType);

    int insertWalletLog(BizWalletLog log);
}
