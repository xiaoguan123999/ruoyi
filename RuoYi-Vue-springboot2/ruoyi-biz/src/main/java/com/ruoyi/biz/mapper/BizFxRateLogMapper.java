package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizFxRateLog;

public interface BizFxRateLogMapper
{
    List<BizFxRateLog> selectFxRateLogList(BizFxRateLog log);

    int insertFxRateLog(BizFxRateLog log);
}
