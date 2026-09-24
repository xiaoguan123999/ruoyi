package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizLotteryPrizePool;

public interface IBizLotteryPrizePoolService
{
    BizLotteryPrizePool selectPrizePoolById(Long poolId);

    List<BizLotteryPrizePool> selectPrizePoolList(BizLotteryPrizePool query);

    List<BizLotteryPrizePool> selectEnabledPrizePoolOptions();

    int insertPrizePool(BizLotteryPrizePool pool);

    int updatePrizePool(BizLotteryPrizePool pool);

    int deletePrizePoolByIds(Long[] poolIds);
}
