package com.ruoyi.biz.api;

public class AppChainDepositOrderResult extends AppDataResult<AppChainDepositOrderData>
{
    public static AppChainDepositOrderResult ok(AppChainDepositOrderData data)
    {
        return fillOk(new AppChainDepositOrderResult(), data);
    }

    public static AppChainDepositOrderResult fail(String message)
    {
        return fillFail(new AppChainDepositOrderResult(), message);
    }
}
