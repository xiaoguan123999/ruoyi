package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizProductCardMetric;

public interface BizProductCardMetricMapper
{
    List<BizProductCardMetric> selectByProductId(Long productId);

    List<BizProductCardMetric> selectByProductIds(@Param("productIds") List<Long> productIds);

    int deleteByProductId(Long productId);

    int insertMetric(BizProductCardMetric metric);

    int insertMetricBatch(@Param("list") List<BizProductCardMetric> list);
}
