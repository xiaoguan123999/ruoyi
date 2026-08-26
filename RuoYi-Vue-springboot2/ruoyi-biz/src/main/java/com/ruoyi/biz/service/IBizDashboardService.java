package com.ruoyi.biz.service;

import com.ruoyi.biz.domain.BizDashboardStats;
import com.ruoyi.biz.domain.BizDashboardTrend;

public interface IBizDashboardService
{
    BizDashboardStats selectStats(String dateText);

    BizDashboardTrend selectTrend(String dateText);
}
