package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizOverview;
import com.ruoyi.biz.mapper.BizOverviewMapper;
import com.ruoyi.biz.service.IBizOverviewService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizOverviewServiceImpl implements IBizOverviewService
{
    @Autowired
    private BizOverviewMapper overviewMapper;

    @Override
    public BizOverview selectOverviewById(Long itemId)
    {
        return overviewMapper.selectOverviewById(itemId);
    }

    @Override
    public List<BizOverview> selectOverviewList(BizOverview overview)
    {
        return overviewMapper.selectOverviewList(overview);
    }

    @Override
    public List<BizOverview> selectAppOverviewList()
    {
        BizOverview query = new BizOverview();
        query.setStatus(BizConstants.STATUS_OK);
        return overviewMapper.selectOverviewList(query);
    }

    @Override
    public int insertOverview(BizOverview overview)
    {
        fillDefaults(overview);
        checkUnique(overview);
        return overviewMapper.insertOverview(overview);
    }

    @Override
    public int updateOverview(BizOverview overview)
    {
        fillDefaults(overview);
        checkUnique(overview);
        return overviewMapper.updateOverview(overview);
    }

    @Override
    public int deleteOverviewByIds(Long[] itemIds)
    {
        return overviewMapper.deleteOverviewByIds(itemIds);
    }

    private void fillDefaults(BizOverview overview)
    {
        if (StringUtils.isEmpty(overview.getStatus()))
        {
            overview.setStatus(BizConstants.STATUS_OK);
        }
        if (StringUtils.isEmpty(overview.getStatusColor()))
        {
            overview.setStatusColor("#4DA3FF");
        }
        if (overview.getImageUrl() == null)
        {
            overview.setImageUrl("");
        }
        if (overview.getStatusText() == null)
        {
            overview.setStatusText("");
        }
        if (overview.getSort() == null)
        {
            overview.setSort(0);
        }
    }

    private void checkUnique(BizOverview overview)
    {
        if (StringUtils.isEmpty(overview.getItemKey()))
        {
            throw new ServiceException("请填写卡片标识");
        }
        if (StringUtils.isEmpty(overview.getTitle()))
        {
            throw new ServiceException("请填写标题");
        }
        if (StringUtils.isEmpty(overview.getDisplayValue()))
        {
            throw new ServiceException("请填写展示数值");
        }
        BizOverview exist = overviewMapper.selectOverviewByItemKey(overview.getItemKey().trim());
        if (exist != null && (overview.getItemId() == null || !exist.getItemId().equals(overview.getItemId())))
        {
            throw new ServiceException("卡片标识已存在");
        }
        overview.setItemKey(overview.getItemKey().trim());
    }
}
