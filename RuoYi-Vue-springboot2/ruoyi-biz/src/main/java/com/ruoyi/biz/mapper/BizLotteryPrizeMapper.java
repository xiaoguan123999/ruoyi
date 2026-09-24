package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.ruoyi.biz.domain.BizLotteryPrize;
import com.ruoyi.biz.domain.BizLotteryPrizePool;

@Mapper
public interface BizLotteryPrizeMapper
{
    BizLotteryPrize selectPrizeById(Long prizeId);

    List<BizLotteryPrize> selectPrizeListByActivityId(Long activityId);

    int deletePrizeByActivityId(Long activityId);

    int insertPrize(BizLotteryPrize prize);

    int updatePrize(BizLotteryPrize prize);

    int countFallbackByActivityId(Long activityId);

    /** 奖品池展示信息变更时，同步到已引用的活动奖项（不含库存/概率/排序） */
    int syncMetaFromPool(BizLotteryPrizePool pool);
}
