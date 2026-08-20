package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.domain.BizCommissionLog;
import com.ruoyi.biz.mapper.BizCommissionLogMapper;
import com.ruoyi.biz.service.IBizCommissionService;

@Service
public class BizCommissionServiceImpl implements IBizCommissionService
{
    @Autowired
    private BizCommissionLogMapper commissionLogMapper;

    @Override
    public List<BizCommissionLog> selectCommissionList(BizCommissionLog log)
    {
        return commissionLogMapper.selectCommissionList(log);
    }
}
