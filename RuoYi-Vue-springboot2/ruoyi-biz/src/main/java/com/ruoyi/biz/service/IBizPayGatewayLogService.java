package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizPayGatewayLog;

public interface IBizPayGatewayLogService
{
    List<BizPayGatewayLog> selectList(BizPayGatewayLog query);

    BizPayGatewayLog selectById(Long logId);
}
