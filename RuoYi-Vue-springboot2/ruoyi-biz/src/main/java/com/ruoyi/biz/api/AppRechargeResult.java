package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.ruoyi.biz.domain.BizRecharge;
import io.swagger.annotations.ApiModel;

@ApiModel("充值申请响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppRechargeResult extends AppDataResult<BizRecharge>
{
    public static AppRechargeResult ok(BizRecharge data)
    {
        return fillOk(new AppRechargeResult(), data);
    }

    public static AppRechargeResult fail(String message)
    {
        return fillFail(new AppRechargeResult(), message);
    }
}
