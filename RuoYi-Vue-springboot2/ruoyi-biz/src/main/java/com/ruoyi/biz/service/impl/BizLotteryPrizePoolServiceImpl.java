package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizLotteryPrizePool;
import com.ruoyi.biz.mapper.BizLotteryPrizeMapper;
import com.ruoyi.biz.mapper.BizLotteryPrizePoolMapper;
import com.ruoyi.biz.service.IBizLotteryPrizePoolService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizLotteryPrizePoolServiceImpl implements IBizLotteryPrizePoolService
{
    @Autowired
    private BizLotteryPrizePoolMapper prizePoolMapper;

    @Autowired
    private BizLotteryPrizeMapper lotteryPrizeMapper;

    @Override
    public BizLotteryPrizePool selectPrizePoolById(Long poolId)
    {
        return prizePoolMapper.selectPrizePoolById(poolId);
    }

    @Override
    public List<BizLotteryPrizePool> selectPrizePoolList(BizLotteryPrizePool query)
    {
        return prizePoolMapper.selectPrizePoolList(query);
    }

    @Override
    public List<BizLotteryPrizePool> selectEnabledPrizePoolOptions()
    {
        return prizePoolMapper.selectEnabledPrizePoolOptions();
    }

    @Override
    public int insertPrizePool(BizLotteryPrizePool pool)
    {
        validatePool(pool);
        fillDefaults(pool, true);
        return prizePoolMapper.insertPrizePool(pool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePrizePool(BizLotteryPrizePool pool)
    {
        if (pool.getPoolId() == null)
        {
            throw new ServiceException("奖品池ID不能为空");
        }
        validatePool(pool);
        fillDefaults(pool, false);
        int rows = prizePoolMapper.updatePrizePool(pool);
        // 展示信息同步到已挂载的活动奖项；库存/概率/排序仍由活动侧维护
        lotteryPrizeMapper.syncMetaFromPool(pool);
        return rows;
    }

    @Override
    public int deletePrizePoolByIds(Long[] poolIds)
    {
        if (poolIds == null || poolIds.length == 0)
        {
            return 0;
        }
        for (int i = 0; i < poolIds.length; i++)
        {
            Long poolId = poolIds[i];
            if (prizePoolMapper.countActivityPrizeByPoolId(poolId) > 0)
            {
                BizLotteryPrizePool pool = prizePoolMapper.selectPrizePoolById(poolId);
                String name = pool != null ? pool.getPrizeName() : String.valueOf(poolId);
                throw new ServiceException("奖品「" + name + "」已被抽奖活动引用，无法删除");
            }
        }
        return prizePoolMapper.deletePrizePoolByIds(poolIds);
    }

    private void validatePool(BizLotteryPrizePool pool)
    {
        if (StringUtils.isEmpty(pool.getPrizeName()))
        {
            throw new ServiceException("请填写奖品名称");
        }
        if (pool.getPrizeType() == null)
        {
            throw new ServiceException("请选择奖品类型");
        }
        int type = pool.getPrizeType().intValue();
        if (type != BizConstants.LOTTERY_PRIZE_PHYSICAL
                && type != BizConstants.LOTTERY_PRIZE_CASH
                && type != BizConstants.LOTTERY_PRIZE_VIRTUAL)
        {
            throw new ServiceException("奖品类型无效");
        }
        if (type == BizConstants.LOTTERY_PRIZE_VIRTUAL)
        {
            if (pool.getAssistAmount() == null)
            {
                pool.setAssistAmount(java.math.BigDecimal.ZERO);
            }
            if (StringUtils.isEmpty(pool.getCurrency()))
            {
                pool.setCurrency("CNY");
            }
        }
        else
        {
            pool.setAssistAmount(null);
            pool.setCurrency(null);
        }
    }

    private void fillDefaults(BizLotteryPrizePool pool, boolean create)
    {
        if (create && StringUtils.isEmpty(pool.getStatus()))
        {
            pool.setStatus(BizConstants.STATUS_OK);
        }
        if (pool.getSort() == null)
        {
            pool.setSort(Integer.valueOf(0));
        }
        if (StringUtils.isEmpty(pool.getSectorBgColor()))
        {
            pool.setSectorBgColor("#FFF8EC");
        }
        if (StringUtils.isEmpty(pool.getNameColor()))
        {
            pool.setNameColor("#111827");
        }
        if (StringUtils.isEmpty(pool.getDescColor()))
        {
            pool.setDescColor("#374151");
        }
    }
}
