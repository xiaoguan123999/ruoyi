package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizLotteryActivity;
import com.ruoyi.biz.domain.BizLotteryPrize;
import com.ruoyi.biz.domain.BizLotteryWinStrategy;

public interface IBizLotteryActivityService
{
    BizLotteryActivity selectLotteryActivityById(Long activityId);

    /** 系统唯一抽奖活动（可为空） */
    BizLotteryActivity selectSoleLotteryActivity();

    List<BizLotteryActivity> selectLotteryActivityList(BizLotteryActivity query);

    int insertLotteryActivity(BizLotteryActivity activity);

    int updateLotteryActivity(BizLotteryActivity activity);

    int deleteLotteryActivityByIds(Long[] activityIds);

    int savePrizes(Long activityId, List<BizLotteryPrize> prizes, String operator);

    int saveStrategies(Long activityId, List<BizLotteryWinStrategy> strategies, String operator);
}
