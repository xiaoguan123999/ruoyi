package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.biz.domain.BizLotteryDrawInfo;
import io.swagger.annotations.ApiModel;

@ApiModel("抽奖活动信息响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLotteryInfoResult extends AppDataResult<BizLotteryDrawInfo>
{
    public static AppLotteryInfoResult ok(BizLotteryDrawInfo data)
    {
        return fillOk(new AppLotteryInfoResult(), data);
    }

    public static AppLotteryInfoResult fail(String message)
    {
        return fillFail(new AppLotteryInfoResult(), message);
    }
}
