package com.ruoyi.biz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.domain.BizBlacklistLog;
import com.ruoyi.biz.mapper.BizBlacklistLogMapper;

@Service
public class BizBlacklistLogRecorder
{
    @Autowired
    private BizBlacklistLogMapper logMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(BizBlacklistLog row)
    {
        logMapper.insertLog(row);
    }
}
