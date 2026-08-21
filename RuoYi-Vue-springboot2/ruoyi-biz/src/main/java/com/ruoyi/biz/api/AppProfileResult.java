package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.ruoyi.biz.domain.BizMember;
import io.swagger.annotations.ApiModel;

@ApiModel("我的资料响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppProfileResult extends AppDataResult<BizMember>
{
    public static AppProfileResult ok(BizMember data)
    {
        return fillOk(new AppProfileResult(), data);
    }

    public static AppProfileResult fail(String message)
    {
        return fillFail(new AppProfileResult(), message);
    }
}
