package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizCheckinPrize;

public interface BizCheckinPrizeMapper
{
    List<BizCheckinPrize> selectPrizeList(BizCheckinPrize prize);

    int insertPrize(BizCheckinPrize prize);
}
