package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizLotteryRecord;

public interface IBizLotteryRecordService
{
    List<BizLotteryRecord> selectRecordList(BizLotteryRecord query);
}
