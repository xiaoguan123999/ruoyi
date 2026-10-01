package com.ruoyi.biz.api;

import java.util.List;

public class AppRechargeMethodListResult extends AppDataResult<List<AppRechargeMethodItem>>
{
    public static AppRechargeMethodListResult ok(List<AppRechargeMethodItem> data)
    {
        return fillOk(new AppRechargeMethodListResult(), data);
    }
}
