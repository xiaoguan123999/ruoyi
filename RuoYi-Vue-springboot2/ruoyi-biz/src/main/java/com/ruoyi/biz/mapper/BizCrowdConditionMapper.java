package com.ruoyi.biz.mapper;


import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizCrowdCondition;

@Mapper
public interface BizCrowdConditionMapper
{
    List<BizCrowdCondition> selectConditionListByPackageId(Long packageId);

    int deleteConditionByPackageId(Long packageId);

    int insertCondition(BizCrowdCondition condition);
}
