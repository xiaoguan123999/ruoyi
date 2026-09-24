package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.domain.BizLotteryRecord;
import com.ruoyi.biz.mapper.BizLotteryRecordMapper;
import com.ruoyi.biz.service.IBizLotteryRecordService;

@Service
public class BizLotteryRecordServiceImpl implements IBizLotteryRecordService
{
    @Autowired
    private BizLotteryRecordMapper lotteryRecordMapper;

    @Override
    public List<BizLotteryRecord> selectRecordList(BizLotteryRecord query)
    {
        return lotteryRecordMapper.selectRecordList(query);
    }
}
