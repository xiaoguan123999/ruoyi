package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.biz.domain.BizWithdrawRule;
import io.swagger.annotations.ApiModel;

@ApiModel("提现规则响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppWithdrawRuleResult extends AppDataResult<BizWithdrawRule>
{
    public static AppWithdrawRuleResult ok(BizWithdrawRule data)
    {
        return fillOk(new AppWithdrawRuleResult(), data);
    }

    public static AppWithdrawRuleResult fail(String message)
    {
        return fillFail(new AppWithdrawRuleResult(), message);
    }
}
