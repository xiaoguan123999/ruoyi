package com.ruoyi.biz.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.biz.domain.BizPayAccount;
import io.swagger.annotations.ApiModel;

@ApiModel("收款账户列表响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPayAccountListResult extends AppDataResult<List<BizPayAccount>>
{
    public static AppPayAccountListResult ok(List<BizPayAccount> data)
    {
        return fillOk(new AppPayAccountListResult(), data);
    }
}
