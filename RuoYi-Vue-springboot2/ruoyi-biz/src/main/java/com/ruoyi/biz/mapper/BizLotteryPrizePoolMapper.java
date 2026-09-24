package com.ruoyi.biz.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizLotteryPrizePool;

@Mapper
public interface BizLotteryPrizePoolMapper
{
    BizLotteryPrizePool selectPrizePoolById(Long poolId);

    List<BizLotteryPrizePool> selectPrizePoolList(BizLotteryPrizePool query);

    List<BizLotteryPrizePool> selectEnabledPrizePoolOptions();

    int insertPrizePool(BizLotteryPrizePool pool);

    int updatePrizePool(BizLotteryPrizePool pool);

    int deletePrizePoolByIds(Long[] poolIds);

    int countActivityPrizeByPoolId(Long poolId);
}
