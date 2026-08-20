package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.mapper.BizLevelMapper;
import com.ruoyi.biz.service.IBizLevelService;

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
