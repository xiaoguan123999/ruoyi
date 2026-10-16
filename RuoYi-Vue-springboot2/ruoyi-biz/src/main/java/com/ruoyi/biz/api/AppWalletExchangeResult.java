package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("钱包互转响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppWalletExchangeResult extends AppDataResult<AppWalletExchangeData>
{
    public static AppWalletExchangeResult ok(AppWalletExchangeData data)
    {
        return fillOk(new AppWalletExchangeResult(), data);
    }
}
