package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCrowdCondition;
import com.ruoyi.biz.domain.BizCrowdPackage;
import com.ruoyi.biz.mapper.BizCrowdConditionMapper;
import com.ruoyi.biz.mapper.BizCrowdPackageMapper;
import com.ruoyi.biz.mapper.BizLotteryWinStrategyMapper;
import com.ruoyi.biz.service.IBizCrowdPackageService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizCrowdPackageServiceImpl implements IBizCrowdPackageService
{
    @Autowired
    private BizCrowdPackageMapper crowdPackageMapper;

    @Autowired
    private BizCrowdConditionMapper crowdConditionMapper;

    @Autowired
    private BizLotteryWinStrategyMapper lotteryWinStrategyMapper;

    @Override
    public BizCrowdPackage selectCrowdPackageById(Long packageId)
    {
        BizCrowdPackage crowdPackage = crowdPackageMapper.selectCrowdPackageById(packageId);
        if (crowdPackage != null)
        {
            crowdPackage.setConditions(crowdConditionMapper.selectConditionListByPackageId(packageId));
        }
        return crowdPackage;
    }

    @Override
    public List<BizCrowdPackage> selectCrowdPackageList(BizCrowdPackage query)
    {
        return crowdPackageMapper.selectCrowdPackageList(query);
    }

    @Override
    public List<BizCrowdPackage> selectEnabledOptions()
    {
        return crowdPackageMapper.selectEnabledOptions();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCrowdPackage(BizCrowdPackage crowdPackage)
    {
        validatePackage(crowdPackage);
        fillDefaults(crowdPackage);
        int rows = crowdPackageMapper.insertCrowdPackage(crowdPackage);
        saveConditions(crowdPackage);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCrowdPackage(BizCrowdPackage crowdPackage)
    {
        if (crowdPackage.getPackageId() == null)
        {
            throw new ServiceException("人群包ID不能为空");
        }
        validatePackage(crowdPackage);
        int rows = crowdPackageMapper.updateCrowdPackage(crowdPackage);
        saveConditions(crowdPackage);
        return rows;
    }

    @Override
    public int deleteCrowdPackageByIds(Long[] packageIds)
    {
        if (packageIds == null || packageIds.length == 0)
        {
            return 0;
        }
        for (int i = 0; i < packageIds.length; i++)
        {
            Long packageId = packageIds[i];
            int used = lotteryWinStrategyMapper.countByPackageId(packageId);
            if (used > 0)
            {
                BizCrowdPackage pkg = crowdPackageMapper.selectCrowdPackageById(packageId);
                String name = pkg != null ? pkg.getPackageName() : String.valueOf(packageId);
                throw new ServiceException("人群包「" + name + "」仍被大转盘必中策略引用，无法删除");
            }
            crowdConditionMapper.deleteConditionByPackageId(packageId);
        }
        return crowdPackageMapper.deleteCrowdPackageByIds(packageIds);
    }

    private void validatePackage(BizCrowdPackage crowdPackage)
    {
        if (StringUtils.isEmpty(crowdPackage.getPackageName()))
        {
            throw new ServiceException("请填写人群包名称");
        }
        if (crowdPackage.getConditions() == null || crowdPackage.getConditions().isEmpty())
        {
            throw new ServiceException("请至少配置一条人群条件");
        }
        for (int i = 0; i < crowdPackage.getConditions().size(); i++)
        {
            BizCrowdCondition condition = crowdPackage.getConditions().get(i);
            if (StringUtils.isEmpty(condition.getLabelType()))
            {
                throw new ServiceException("第" + (i + 1) + "条条件缺少标签维度");
            }
            if (StringUtils.isEmpty(condition.getLabelField()))
            {
                throw new ServiceException("第" + (i + 1) + "条条件缺少标签字段");
            }
            if (StringUtils.isEmpty(condition.getOperatorType()))
            {
                throw new ServiceException("第" + (i + 1) + "条条件缺少运算符");
            }
            if (StringUtils.isEmpty(condition.getRuleValue()))
            {
                throw new ServiceException("第" + (i + 1) + "条条件缺少限制值");
            }
        }
    }

    private void fillDefaults(BizCrowdPackage crowdPackage)
    {
        if (StringUtils.isEmpty(crowdPackage.getStatus()))
        {
            crowdPackage.setStatus(BizConstants.STATUS_OK);
        }
    }

    private void saveConditions(BizCrowdPackage crowdPackage)
    {
        Long packageId = crowdPackage.getPackageId();
        crowdConditionMapper.deleteConditionByPackageId(packageId);
        List<BizCrowdCondition> conditions = crowdPackage.getConditions();
        for (int i = 0; i < conditions.size(); i++)
        {
            BizCrowdCondition condition = conditions.get(i);
            condition.setPackageId(packageId);
            if (condition.getSort() == null)
            {
                condition.setSort(Integer.valueOf(i));
            }
            crowdConditionMapper.insertCondition(condition);
        }
    }
}
