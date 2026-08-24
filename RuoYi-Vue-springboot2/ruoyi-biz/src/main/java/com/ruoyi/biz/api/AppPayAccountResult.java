package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.biz.domain.BizPayAccount;
import io.swagger.annotations.ApiModel;

@ApiModel("收款账户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPayAccountResult extends AppDataResult<BizPayAccount>
{
    public static AppPayAccountResult ok(BizPayAccount data)
    {
        return fillOk(new AppPayAccountResult(), data);
    }
}
