package com.ruoyi.biz.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizLotteryWinStrategy;

@Mapper
public interface BizLotteryWinStrategyMapper
{
    List<BizLotteryWinStrategy> selectStrategyListByActivityId(Long activityId);

    int deleteStrategyByActivityId(Long activityId);

    int insertStrategy(BizLotteryWinStrategy strategy);

    int updateStrategy(BizLotteryWinStrategy strategy);

    int countByPackageId(Long packageId);
}
