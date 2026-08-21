package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("产品列表响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppProductListResult extends AppDataResult<java.util.List<com.ruoyi.biz.domain.BizProduct>>
{
    public static AppProductListResult ok(java.util.List<com.ruoyi.biz.domain.BizProduct> data)
    {
        return fillOk(new AppProductListResult(), data);
    }

    public static AppProductListResult fail(String message)
    {
        return fillFail(new AppProductListResult(), message);
    }
}
