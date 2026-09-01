package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizAppVersion;

public interface BizAppVersionMapper
{
    BizAppVersion selectVersionById(Long versionId);

    List<BizAppVersion> selectVersionList(BizAppVersion query);

    BizAppVersion selectLatest(@Param("platform") String platform);

    BizAppVersion selectDuplicate(@Param("platform") String platform, @Param("version") String version,
            @Param("versionId") Long versionId);

    int clearLatest(@Param("platform") String platform, @Param("excludeId") Long excludeId);

    int insertVersion(BizAppVersion row);

    int updateVersion(BizAppVersion row);

    int deleteVersionByIds(Long[] versionIds);
}
