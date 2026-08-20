package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizCommissionLog;

public interface BizCommissionLogMapper
{
    List<BizCommissionLog> selectCommissionList(BizCommissionLog log);

    int insertCommissionLog(BizCommissionLog log);
}
