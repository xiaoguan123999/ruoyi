package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.domain.BizAppVersion;
import com.ruoyi.biz.mapper.BizAppVersionMapper;
import com.ruoyi.biz.service.IBizAppVersionService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizAppVersionServiceImpl implements IBizAppVersionService
{
    @Autowired
    private BizAppVersionMapper versionMapper;

    @Override
    public BizAppVersion selectVersionById(Long versionId)
    {
        return versionMapper.selectVersionById(versionId);
    }

    @Override
    public List<BizAppVersion> selectVersionList(BizAppVersion query)
    {
        if (query != null)
        {
            query.setPlatform(normPlatform(query.getPlatform(), false));
            query.setIsLatest(BizAppVersion.norm(query.getIsLatest()));
            query.setIsEnabled(BizAppVersion.norm(query.getIsEnabled()));
            query.setForceUpdate(BizAppVersion.norm(query.getForceUpdate()));
        }
        return versionMapper.selectVersionList(query == null ? new BizAppVersion() : query);
    }

    @Override
    public BizAppVersion selectLatest(String platform)
    {
        String p = normPlatform(platform, true);
        return versionMapper.selectLatest(p);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertVersion(BizAppVersion row)
    {
        fill(row, true);
        assertUnique(row);
        if (row.latest())
        {
            versionMapper.clearLatest(row.getPlatform(), null);
        }
        return versionMapper.insertVersion(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVersion(BizAppVersion row)
    {
        if (row.getVersionId() == null)
        {
            throw new ServiceException("请选择版本");
        }
        BizAppVersion old = require(row.getVersionId());
        fill(row, false);
        if (StringUtils.isEmpty(row.getPlatform()))
        {
            row.setPlatform(old.getPlatform());
        }
        if (StringUtils.isEmpty(row.getVersion()))
        {
            row.setVersion(old.getVersion());
        }
        assertUnique(row);
        if (row.latest())
        {
            versionMapper.clearLatest(row.getPlatform(), row.getVersionId());
        }
        return versionMapper.updateVersion(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setLatest(Long versionId, boolean isLatest)
    {
        BizAppVersion old = require(versionId);
        if (isLatest)
        {
            versionMapper.clearLatest(old.getPlatform(), versionId);
        }
        BizAppVersion upd = new BizAppVersion();
        upd.setVersionId(versionId);
        upd.setIsLatest(isLatest ? "1" : "0");
        return versionMapper.updateVersion(upd);
    }

    @Override
    public int setForceUpdate(Long versionId, boolean forceUpdate)
    {
        require(versionId);
        BizAppVersion upd = new BizAppVersion();
        upd.setVersionId(versionId);
        upd.setForceUpdate(forceUpdate ? "1" : "0");
        return versionMapper.updateVersion(upd);
    }

    @Override
    public int setEnabled(Long versionId, boolean isEnabled)
    {
        require(versionId);
        BizAppVersion upd = new BizAppVersion();
        upd.setVersionId(versionId);
        upd.setIsEnabled(isEnabled ? "1" : "0");
        return versionMapper.updateVersion(upd);
    }

    @Override
    public int deleteVersionByIds(Long[] versionIds)
    {
        if (versionIds == null || versionIds.length == 0)
        {
            throw new ServiceException("请选择版本");
        }
        return versionMapper.deleteVersionByIds(versionIds);
    }

    private BizAppVersion require(Long versionId)
    {
        BizAppVersion row = versionMapper.selectVersionById(versionId);
        if (row == null)
        {
            throw new ServiceException("版本不存在");
        }
        return row;
    }

    private void assertUnique(BizAppVersion row)
    {
        BizAppVersion dup = versionMapper.selectDuplicate(row.getPlatform(), row.getVersion(), row.getVersionId());
        if (dup != null)
        {
            throw new ServiceException("该平台已存在此版本号");
        }
    }

    private void fill(BizAppVersion row, boolean insert)
    {
        row.setPlatform(normPlatform(row.getPlatform(), true));
        if (StringUtils.isEmpty(row.getVersion()))
        {
            throw new ServiceException("请填写版本号");
        }
        row.setVersion(row.getVersion().trim());
        if (insert && StringUtils.isEmpty(row.getDownloadUrl()))
        {
            throw new ServiceException("请填写下载链接");
        }
        if (row.getDescription() == null)
        {
            row.setDescription("");
        }
        if (row.getForceUpdate() == null)
        {
            row.setForceUpdate(insert ? "0" : null);
        }
        if (row.getIsLatest() == null)
        {
            row.setIsLatest(insert ? "0" : null);
        }
        if (row.getIsEnabled() == null)
        {
            row.setIsEnabled(insert ? "1" : null);
        }
        if (row.getSortOrder() == null && insert)
        {
            row.setSortOrder(Integer.valueOf(0));
        }
    }

    private String normPlatform(String platform, boolean required)
    {
        if (StringUtils.isEmpty(platform))
        {
            if (required)
            {
                throw new ServiceException("请选择平台 android 或 ios");
            }
            return platform;
        }
        String p = platform.trim().toLowerCase();
        if ("android".equals(p) || "ios".equals(p))
        {
            return p;
        }
        throw new ServiceException("平台只能是 android 或 ios");
    }
}
