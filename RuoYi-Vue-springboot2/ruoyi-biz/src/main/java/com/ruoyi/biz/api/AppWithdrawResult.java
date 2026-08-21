package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.ruoyi.biz.domain.BizWithdraw;
import io.swagger.annotations.ApiModel;

@ApiModel("提现申请响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppWithdrawResult extends AppDataResult<BizWithdraw>
{
    public static AppWithdrawResult ok(BizWithdraw data)
    {
        return fillOk(new AppWithdrawResult(), data);
    }

    public static AppWithdrawResult fail(String message)
    {
        return fillFail(new AppWithdrawResult(), message);
    }
}
