package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizPromoGrant;

public interface BizPromoGrantMapper
{
    BizPromoGrant selectByTypeAndFrom(@Param("grantType") String grantType, @Param("fromMemberId") Long fromMemberId);

    List<BizPromoGrant> selectGrantList(BizPromoGrant grant);

    int insertGrant(BizPromoGrant grant);
}
