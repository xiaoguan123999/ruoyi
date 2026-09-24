package com.ruoyi.biz.service;

import com.ruoyi.biz.domain.BizLotteryDrawInfo;
import com.ruoyi.biz.domain.BizLotteryDrawResult;

/**
 * 大转盘抽奖引擎
 */
public interface IBizLotteryDrawService
{
    BizLotteryDrawInfo getCurrentInfo(Long memberId);

    BizLotteryDrawResult draw(Long memberId);
}
