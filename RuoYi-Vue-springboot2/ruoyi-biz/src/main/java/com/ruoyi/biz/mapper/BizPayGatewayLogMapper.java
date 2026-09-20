package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.ruoyi.biz.domain.BizPayGatewayLog;

@Mapper
public interface BizPayGatewayLogMapper
{
    BizPayGatewayLog selectById(Long logId);

    List<BizPayGatewayLog> selectList(BizPayGatewayLog query);

    int insert(BizPayGatewayLog row);
}
