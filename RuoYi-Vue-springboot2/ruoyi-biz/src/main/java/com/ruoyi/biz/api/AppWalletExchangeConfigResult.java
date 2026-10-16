package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("钱包互转配置响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppWalletExchangeConfigResult extends AppDataResult<AppWalletExchangeConfig>
{
    public static AppWalletExchangeConfigResult ok(AppWalletExchangeConfig data)
    {
        return fillOk(new AppWalletExchangeConfigResult(), data);
    }
}
