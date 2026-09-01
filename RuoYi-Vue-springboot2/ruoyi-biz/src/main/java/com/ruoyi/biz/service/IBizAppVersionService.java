package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizAppVersion;

public interface IBizAppVersionService
{
    BizAppVersion selectVersionById(Long versionId);

    List<BizAppVersion> selectVersionList(BizAppVersion query);

    BizAppVersion selectLatest(String platform);

    int insertVersion(BizAppVersion row);

    int updateVersion(BizAppVersion row);

    int setLatest(Long versionId, boolean isLatest);

    int setForceUpdate(Long versionId, boolean forceUpdate);

    int setEnabled(Long versionId, boolean isEnabled);

    int deleteVersionByIds(Long[] versionIds);
}
