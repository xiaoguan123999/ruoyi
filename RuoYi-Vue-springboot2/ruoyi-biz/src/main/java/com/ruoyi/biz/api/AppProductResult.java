package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.biz.domain.BizProduct;
import io.swagger.annotations.ApiModel;

@ApiModel("产品详情响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppProductResult extends AppDataResult<BizProduct>
{
    public static AppProductResult ok(BizProduct data)
    {
        return fillOk(new AppProductResult(), data);
    }

    public static AppProductResult fail(String message)
    {
        return fillFail(new AppProductResult(), message);
    }
}
