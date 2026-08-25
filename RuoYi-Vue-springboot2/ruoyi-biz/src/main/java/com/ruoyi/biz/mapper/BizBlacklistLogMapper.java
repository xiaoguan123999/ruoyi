package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizBlacklistLog;

public interface BizBlacklistLogMapper
{
    List<BizBlacklistLog> selectLogList(BizBlacklistLog query);

    int insertLog(BizBlacklistLog row);

    int deleteLogByIds(Long[] logIds);
}
