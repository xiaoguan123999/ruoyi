package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.mapper.BizLevelMapper;
import com.ruoyi.biz.service.IBizLevelService;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizLevelServiceImpl implements IBizLevelService
{
    @Autowired
    private BizLevelMapper levelMapper;

    @Override
    public BizLevel selectLevelById(Long levelId)
    {
        return levelMapper.selectLevelById(levelId);
    }

    @Override
    public List<BizLevel> selectLevelList(BizLevel level)
    {
        return levelMapper.selectLevelList(level);
    }

    @Override
    public int insertLevel(BizLevel level)
    {
        if (level.getStatus() == null)
        {
            level.setStatus(BizConstants.STATUS_OK);
        }
        if (level.getTeamDepth() == null)
        {
            level.setTeamDepth("");
        }
        if (level.getMinTeamPerfCny() == null)
        {
            level.setMinTeamPerfCny(BigDecimal.ZERO);
        }
        if (level.getMinTeamPerfUsdt() == null)
        {
            level.setMinTeamPerfUsdt(BigDecimal.ZERO);
        }
        if (StringUtils.isEmpty(level.getRewardEnabled()))
        {
            level.setRewardEnabled("0");
        }
        if (StringUtils.isEmpty(level.getRewardCycle()))
        {
            level.setRewardCycle("NONE");
        }
        if (StringUtils.isEmpty(level.getRewardMode()))
        {
            level.setRewardMode("AUTO");
        }
        if (StringUtils.isEmpty(level.getRewardRepeat()))
        {
            level.setRewardRepeat("NONE");
        }
        if (level.getRewardCny() == null)
        {
            level.setRewardCny(BigDecimal.ZERO);
        }
        if (level.getRewardUsdt() == null)
        {
            level.setRewardUsdt(BigDecimal.ZERO);
        }
        return levelMapper.insertLevel(level);
    }

    @Override
    public int updateLevel(BizLevel level)
    {
        return levelMapper.updateLevel(level);
    }

    @Override
    public int deleteLevelByIds(Long[] levelIds)
    {
        return levelMapper.deleteLevelByIds(levelIds);
    }
}
