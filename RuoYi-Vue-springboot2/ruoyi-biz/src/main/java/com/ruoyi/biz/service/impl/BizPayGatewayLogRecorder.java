package com.ruoyi.biz.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.domain.BizPayGatewayLog;
import com.ruoyi.biz.mapper.BizPayGatewayLogMapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * Persists pay gateway logs in a new transaction so place-order rollback cannot erase them.
 */
@Service
public class BizPayGatewayLogRecorder
{
    private static final Logger log = LoggerFactory.getLogger(BizPayGatewayLogRecorder.class);

    @Autowired
    private BizPayGatewayLogMapper logMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(BizPayGatewayLog row)
    {
        if (row == null)
        {
            return;
        }
        try
        {
            row.setRequestUrl(cut(row.getRequestUrl(), 500));
            row.setRequestBody(cut(row.getRequestBody(), 4000));
            row.setResponseBody(cut(row.getResponseBody(), 4000));
            row.setErrorMsg(cut(row.getErrorMsg(), 500));
            row.setProviderCode(nvl(row.getProviderCode()));
            row.setChannelCode(nvl(row.getChannelCode()));
            row.setOutTradeNo(nvl(row.getOutTradeNo()));
            row.setClientIp(nvl(row.getClientIp()));
            row.setAction(nvl(row.getAction()));
            if (StringUtils.isEmpty(row.getSuccess()))
            {
                row.setSuccess("0");
            }
            logMapper.insert(row);
        }
        catch (Exception e)
        {
            log.warn("pay gateway log persist fail: {}", e.getMessage());
        }
    }

    private static String nvl(String raw)
    {
        return raw == null ? "" : raw;
    }

    private static String cut(String raw, int max)
    {
        if (raw == null)
        {
            return "";
        }
        return raw.length() <= max ? raw : raw.substring(0, max);
    }
}
