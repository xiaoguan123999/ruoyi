package com.ruoyi.biz.api;

public class AppPayDepositResult extends AppDataResult<AppPayDepositData>
{
    public static AppPayDepositResult ok(AppPayDepositData data)
    {
        return fillOk(new AppPayDepositResult(), data);
    }

    public static AppPayDepositResult fail(String message)
    {
        return fillFail(new AppPayDepositResult(), message);
    }
}
