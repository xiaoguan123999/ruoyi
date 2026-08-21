package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.ruoyi.biz.domain.BizOrder;
import io.swagger.annotations.ApiModel;

@ApiModel("认购响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppOrderResult extends AppDataResult<BizOrder>
{
    public static AppOrderResult ok(BizOrder data)
    {
        return fillOk(new AppOrderResult(), data);
    }

    public static AppOrderResult fail(String message)
    {
        return fillFail(new AppOrderResult(), message);
    }
}
