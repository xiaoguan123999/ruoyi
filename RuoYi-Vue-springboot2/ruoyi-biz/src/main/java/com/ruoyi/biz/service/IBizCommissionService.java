package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizCommissionLog;

public interface IBizCommissionService
{
    List<BizCommissionLog> selectCommissionList(BizCommissionLog log);
}
