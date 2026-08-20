package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizCheckin;
import com.ruoyi.biz.domain.BizCheckinPrize;
import com.ruoyi.biz.domain.CheckinResult;
import com.ruoyi.biz.domain.CheckinRule;

public interface IBizCheckinService
{
    List<BizCheckin> selectCheckinList(BizCheckin checkin);

    CheckinResult checkin(Long memberId);

    CheckinResult getCheckinInfo(Long memberId);

    CheckinRule getCheckinRule();

    void saveCheckinRule(CheckinRule rule);

    List<BizCheckinPrize> selectPrizeList(BizCheckinPrize prize);
}
