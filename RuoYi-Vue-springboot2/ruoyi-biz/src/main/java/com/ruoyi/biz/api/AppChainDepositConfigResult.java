package com.ruoyi.biz.api;

public class AppChainDepositConfigResult extends AppDataResult<AppChainDepositConfigData>
{
    public static AppChainDepositConfigResult ok(AppChainDepositConfigData data)
    {
        return fillOk(new AppChainDepositConfigResult(), data);
    }
}
