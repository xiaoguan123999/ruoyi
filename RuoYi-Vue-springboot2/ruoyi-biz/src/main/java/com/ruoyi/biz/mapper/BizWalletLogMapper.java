package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizWalletLog;

public interface BizWalletLogMapper
{
    List<BizWalletLog> selectWalletLogList(BizWalletLog log);

    int insertWalletLog(BizWalletLog log);
}
