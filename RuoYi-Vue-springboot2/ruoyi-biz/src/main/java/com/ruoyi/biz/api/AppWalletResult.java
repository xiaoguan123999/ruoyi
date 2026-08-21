package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("钱包资产卡响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppWalletResult extends AppDataResult<AppWalletCard>
{
    public static AppWalletResult ok(AppWalletCard data)
    {
        return fillOk(new AppWalletResult(), data);
    }

    public static AppWalletResult fail(String message)
    {
        return fillFail(new AppWalletResult(), message);
    }
}
