package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.biz.domain.BizLotteryDrawResult;
import io.swagger.annotations.ApiModel;

@ApiModel("大转盘抽奖响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLotteryResult extends AppDataResult<BizLotteryDrawResult>
{
    public static AppLotteryResult ok(BizLotteryDrawResult data)
    {
        return fillOk(new AppLotteryResult(), data);
    }

    public static AppLotteryResult fail(String message)
    {
        return fillFail(new AppLotteryResult(), message);
    }
}
