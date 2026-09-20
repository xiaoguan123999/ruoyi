package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.domain.BizPayGatewayLog;
import com.ruoyi.biz.mapper.BizPayGatewayLogMapper;
import com.ruoyi.biz.service.IBizPayGatewayLogService;

@Service
public class BizPayGatewayLogServiceImpl implements IBizPayGatewayLogService
{
    @Autowired
    private BizPayGatewayLogMapper logMapper;

    @Override
    public List<BizPayGatewayLog> selectList(BizPayGatewayLog query)
    {
        return logMapper.selectList(query == null ? new BizPayGatewayLog() : query);
    }

    @Override
    public BizPayGatewayLog selectById(Long logId)
    {
        if (logId == null)
        {
            return null;
        }
        return logMapper.selectById(logId);
    }
}
