package com.ruoyi.biz.mapper;


import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizCrowdPackage;

@Mapper
public interface BizCrowdPackageMapper
{
    BizCrowdPackage selectCrowdPackageById(Long packageId);

    List<BizCrowdPackage> selectCrowdPackageList(BizCrowdPackage query);

    List<BizCrowdPackage> selectEnabledOptions();

    int insertCrowdPackage(BizCrowdPackage crowdPackage);

    int updateCrowdPackage(BizCrowdPackage crowdPackage);

    int deleteCrowdPackageByIds(Long[] packageIds);
}
