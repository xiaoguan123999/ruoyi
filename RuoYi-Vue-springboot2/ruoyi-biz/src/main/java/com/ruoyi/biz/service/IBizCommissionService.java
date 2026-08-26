package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizCommissionLog;
import com.ruoyi.biz.domain.BizOrder;

public interface IBizCommissionService
{
    List<BizCommissionLog> selectCommissionList(BizCommissionLog log);

    void grantForSubscribe(BizOrder order);
}
