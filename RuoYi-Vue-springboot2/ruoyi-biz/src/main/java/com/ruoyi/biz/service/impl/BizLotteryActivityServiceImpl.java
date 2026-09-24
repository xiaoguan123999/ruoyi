package com.ruoyi.biz.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizLotteryActivity;
import com.ruoyi.biz.domain.BizLotteryPrize;
import com.ruoyi.biz.domain.BizLotteryPrizePool;
import com.ruoyi.biz.domain.BizLotteryWinStrategy;
import com.ruoyi.biz.mapper.BizLotteryActivityMapper;
import com.ruoyi.biz.mapper.BizLotteryPrizeMapper;
import com.ruoyi.biz.mapper.BizLotteryPrizePoolMapper;
import com.ruoyi.biz.mapper.BizLotteryWinStrategyMapper;
import com.ruoyi.biz.service.IBizLotteryActivityService;
import com.ruoyi.biz.service.IBizLotteryStockService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizLotteryActivityServiceImpl implements IBizLotteryActivityService
{
    private static final Logger log = LoggerFactory.getLogger(BizLotteryActivityServiceImpl.class);

    @Autowired
    private BizLotteryActivityMapper lotteryActivityMapper;

    @Autowired
    private BizLotteryPrizeMapper lotteryPrizeMapper;

    @Autowired
    private BizLotteryPrizePoolMapper lotteryPrizePoolMapper;

    @Autowired
    private BizLotteryWinStrategyMapper lotteryWinStrategyMapper;

    @Autowired
    private IBizLotteryStockService lotteryStockService;

    @Override
    public BizLotteryActivity selectLotteryActivityById(Long activityId)
    {
        BizLotteryActivity activity = lotteryActivityMapper.selectLotteryActivityById(activityId);
        if (activity != null)
        {
            activity.setPrizes(lotteryPrizeMapper.selectPrizeListByActivityId(activityId));
            activity.setStrategies(lotteryWinStrategyMapper.selectStrategyListByActivityId(activityId));
        }
        return activity;
    }

    @Override
    public BizLotteryActivity selectSoleLotteryActivity()
    {
        BizLotteryActivity activity = lotteryActivityMapper.selectSoleLotteryActivity();
        if (activity != null)
        {
            Long activityId = activity.getActivityId();
            activity.setPrizes(lotteryPrizeMapper.selectPrizeListByActivityId(activityId));
            activity.setStrategies(lotteryWinStrategyMapper.selectStrategyListByActivityId(activityId));
        }
        return activity;
    }

    @Override
    public List<BizLotteryActivity> selectLotteryActivityList(BizLotteryActivity query)
    {
        return lotteryActivityMapper.selectLotteryActivityList(query);
    }

    @Override
    public int insertLotteryActivity(BizLotteryActivity activity)
    {
        if (lotteryActivityMapper.countLotteryActivity() > 0)
        {
            throw new ServiceException("系统仅支持配置一个抽奖活动，请直接修改现有活动");
        }
        validateActivity(activity);
        fillActivityDefaults(activity, true);
        return lotteryActivityMapper.insertLotteryActivity(activity);
    }

    @Override
    public int updateLotteryActivity(BizLotteryActivity activity)
    {
        if (activity.getActivityId() == null)
        {
            throw new ServiceException("活动ID不能为空");
        }
        validateActivity(activity);
        int rows = lotteryActivityMapper.updateLotteryActivity(activity);
        if (rows > 0 && "0".equals(activity.getStatus()))
        {
            lotteryStockService.warmActivityStock(activity.getActivityId());
        }
        return rows;
    }

    @Override
    public int deleteLotteryActivityByIds(Long[] activityIds)
    {
        if (activityIds == null || activityIds.length == 0)
        {
            return 0;
        }
        for (int i = 0; i < activityIds.length; i++)
        {
            Long activityId = activityIds[i];
            lotteryPrizeMapper.deletePrizeByActivityId(activityId);
            lotteryWinStrategyMapper.deleteStrategyByActivityId(activityId);
        }
        return lotteryActivityMapper.deleteLotteryActivityByIds(activityIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int savePrizes(Long activityId, List<BizLotteryPrize> prizes, String operator)
    {
        if (activityId == null)
        {
            throw new ServiceException("活动ID不能为空");
        }
        if (lotteryActivityMapper.selectLotteryActivityById(activityId) == null)
        {
            throw new ServiceException("活动不存在");
        }
        if (prizes == null || prizes.isEmpty())
        {
            throw new ServiceException("请至少配置一个奖品");
        }
        for (int i = 0; i < prizes.size(); i++)
        {
            resolvePrizeFromPool(prizes.get(i));
        }
        validatePrizes(prizes);
        lotteryPrizeMapper.deletePrizeByActivityId(activityId);
        for (int i = 0; i < prizes.size(); i++)
        {
            BizLotteryPrize prize = prizes.get(i);
            fillPrizeDefaults(prize, activityId, operator, i);
            lotteryPrizeMapper.insertPrize(prize);
        }
        lotteryStockService.warmActivityStock(activityId);
        return prizes.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveStrategies(Long activityId, List<BizLotteryWinStrategy> strategies, String operator)
    {
        if (activityId == null)
        {
            throw new ServiceException("活动ID不能为空");
        }
        if (lotteryActivityMapper.selectLotteryActivityById(activityId) == null)
        {
            throw new ServiceException("活动不存在");
        }
        if (strategies == null)
        {
            strategies = java.util.Collections.emptyList();
        }
        validateStrategies(strategies);
        lotteryWinStrategyMapper.deleteStrategyByActivityId(activityId);
        for (int i = 0; i < strategies.size(); i++)
        {
            BizLotteryWinStrategy strategy = strategies.get(i);
            fillStrategyDefaults(strategy, activityId, operator);
            lotteryWinStrategyMapper.insertStrategy(strategy);
        }
        return strategies.size();
    }

    private void validateActivity(BizLotteryActivity activity)
    {
        if (StringUtils.isEmpty(activity.getTitle()))
        {
            throw new ServiceException("请填写活动名称");
        }
        if (activity.getStartTime() != null && activity.getEndTime() != null
                && activity.getEndTime().before(activity.getStartTime()))
        {
            throw new ServiceException("结束时间不能早于开始时间");
        }
        if (activity.getIntervalHours() == null || activity.getIntervalHours().intValue() < 0)
        {
            throw new ServiceException("频控间隔不能为负数；填 0 表示关闭频控");
        }
    }

    private void fillActivityDefaults(BizLotteryActivity activity, boolean create)
    {
        if (create && StringUtils.isEmpty(activity.getStatus()))
        {
            activity.setStatus(BizConstants.STATUS_DISABLE);
        }
        if (activity.getIntervalHours() == null)
        {
            activity.setIntervalHours(Integer.valueOf(72));
        }
    }

    private void validatePrizes(List<BizLotteryPrize> prizes)
    {
        if (prizes == null || prizes.isEmpty())
        {
            throw new ServiceException("请至少配置一个奖品");
        }
        Set<Integer> positions = new HashSet<Integer>();
        Set<Long> poolIds = new HashSet<Long>();
        int fallbackCount = 0;
        int probabilitySum = 0;
        for (int i = 0; i < prizes.size(); i++)
        {
            BizLotteryPrize prize = prizes.get(i);
            if (prize.getPoolId() == null)
            {
                throw new ServiceException("第" + (i + 1) + "个奖项请选择奖品池奖品");
            }
            if (StringUtils.isEmpty(prize.getPrizeName()))
            {
                throw new ServiceException("第" + (i + 1) + "个奖品缺少名称");
            }
            if (prize.getPrizeType() == null)
            {
                throw new ServiceException("第" + (i + 1) + "个奖品缺少类型");
            }
            if (prize.getPosition() == null || prize.getPosition().intValue() < 1)
            {
                throw new ServiceException("第" + (i + 1) + "个奖品排序须从1起");
            }
            if (prize.getPosition().intValue() > 36)
            {
                throw new ServiceException("第" + (i + 1) + "个奖品排序过大，建议不超过36");
            }
            if (!positions.add(prize.getPosition()))
            {
                throw new ServiceException("排序号" + prize.getPosition() + "重复，每个奖品排序须唯一");
            }
            if (prize.getPoolId() != null && !poolIds.add(prize.getPoolId()))
            {
                throw new ServiceException("同一奖品不能重复加入活动转盘");
            }
            int prizeType = prize.getPrizeType().intValue();
            Integer publicStock = prize.getPublicStock() != null ? prize.getPublicStock() : Integer.valueOf(0);
            Integer exclusiveStock = prize.getExclusiveStock() != null ? prize.getExclusiveStock() : Integer.valueOf(0);
            if (prizeType == BizConstants.LOTTERY_PRIZE_PHYSICAL || prizeType == BizConstants.LOTTERY_PRIZE_CASH)
            {
                if (publicStock.intValue() == -1 || exclusiveStock.intValue() == -1)
                {
                    throw new ServiceException("实物/现金奖品「" + prize.getPrizeName() + "」库存不能设为无限(-1)");
                }
            }
            if ("1".equals(prize.getIsFallback()))
            {
                fallbackCount++;
                if (prizeType != BizConstants.LOTTERY_PRIZE_VIRTUAL)
                {
                    throw new ServiceException("兜底奖品须为虚拟资产类型");
                }
                if (!isFallbackStockValid(publicStock, exclusiveStock))
                {
                    throw new ServiceException("兜底奖品「" + prize.getPrizeName() + "」普通库存必须为无限(-1)");
                }
            }
            if (prize.getProbability() != null)
            {
                probabilitySum += prize.getProbability().intValue();
            }
        }
        if (fallbackCount != 1)
        {
            throw new ServiceException("须恰好配置1个兜底奖品");
        }
        if (probabilitySum != 10000)
        {
            log.warn("活动奖品抽奖概率合计为{}，建议调整为10000(万分制)", Integer.valueOf(probabilitySum));
        }
    }

    private boolean isFallbackStockValid(Integer publicStock, Integer exclusiveStock)
    {
        // 兜底奖普通库存必须无限，否则目标奖抽空后会熔断
        return publicStock != null && publicStock.intValue() == -1;
    }

    private void resolvePrizeFromPool(BizLotteryPrize prize)
    {
        if (prize.getPoolId() == null)
        {
            throw new ServiceException("请选择奖品池中的奖品");
        }
        BizLotteryPrizePool pool = lotteryPrizePoolMapper.selectPrizePoolById(prize.getPoolId());
        if (pool == null)
        {
            throw new ServiceException("奖品池不存在：" + prize.getPoolId());
        }
        if (StringUtils.isNotEmpty(pool.getStatus()) && !"0".equals(pool.getStatus()))
        {
            throw new ServiceException("奖品「" + pool.getPrizeName() + "」已停用，无法配置到活动");
        }
        prize.setPrizeName(pool.getPrizeName());
        prize.setPrizeDesc(pool.getPrizeDesc());
        prize.setImageUrl(pool.getImageUrl());
        prize.setSectorBgColor(pool.getSectorBgColor());
        prize.setNameColor(pool.getNameColor());
        prize.setDescColor(pool.getDescColor());
        prize.setPrizeType(pool.getPrizeType());
        prize.setAssistAmount(pool.getAssistAmount());
        prize.setCurrency(pool.getCurrency());
    }

    private void fillPrizeDefaults(BizLotteryPrize prize, Long activityId, String operator, int index)
    {
        prize.setActivityId(activityId);
        prize.setCreateBy(operator);
        if (prize.getPublicStock() == null)
        {
            prize.setPublicStock(Integer.valueOf(0));
        }
        if (prize.getExclusiveStock() == null)
        {
            prize.setExclusiveStock(Integer.valueOf(0));
        }
        if (prize.getProbability() == null)
        {
            prize.setProbability(Integer.valueOf(0));
        }
        if (StringUtils.isEmpty(prize.getIsFallback()))
        {
            prize.setIsFallback("0");
        }
        if (StringUtils.isEmpty(prize.getCurrency()))
        {
            prize.setCurrency(BizConstants.CURRENCY_CNY);
        }
        if (prize.getSort() == null)
        {
            prize.setSort(Integer.valueOf(index));
        }
        if (StringUtils.isEmpty(prize.getStatus()))
        {
            prize.setStatus(BizConstants.STATUS_OK);
        }
    }

    private void validateStrategies(List<BizLotteryWinStrategy> strategies)
    {
        for (int i = 0; i < strategies.size(); i++)
        {
            BizLotteryWinStrategy strategy = strategies.get(i);
            if (strategy.getTargetType() == null)
            {
                throw new ServiceException("第" + (i + 1) + "条策略缺少目标类型");
            }
            int targetType = strategy.getTargetType().intValue();
            if (targetType != BizConstants.LOTTERY_TARGET_ALL
                    && targetType != BizConstants.LOTTERY_TARGET_USER
                    && targetType != BizConstants.LOTTERY_TARGET_CROWD)
            {
                throw new ServiceException("第" + (i + 1) + "条策略目标类型无效");
            }
            if (targetType == BizConstants.LOTTERY_TARGET_USER && StringUtils.isEmpty(strategy.getUserIds()))
            {
                throw new ServiceException("第" + (i + 1) + "条策略须填写指定用户ID");
            }
            if (targetType == BizConstants.LOTTERY_TARGET_CROWD && strategy.getPackageId() == null)
            {
                throw new ServiceException("第" + (i + 1) + "条策略须选择人群包");
            }
            if (strategy.getTargetPrizeId() == null)
            {
                throw new ServiceException("第" + (i + 1) + "条策略须指定必中奖品");
            }
            if (strategy.getTriggerCount() == null || strategy.getTriggerCount().intValue() < 1)
            {
                throw new ServiceException("第" + (i + 1) + "条策略触发次数须大于等于1");
            }
            String loopMode = strategy.getLoopMode();
            if (StringUtils.isNotEmpty(loopMode)
                    && !BizConstants.LOTTERY_LOOP_ONCE.equals(loopMode)
                    && !BizConstants.LOTTERY_LOOP_CYCLE.equals(loopMode))
            {
                throw new ServiceException("第" + (i + 1) + "条策略触发模式无效");
            }
            String stockMode = strategy.getStockMode();
            if (StringUtils.isNotEmpty(stockMode)
                    && !BizConstants.LOTTERY_STOCK_AUTO.equals(stockMode)
                    && !BizConstants.LOTTERY_STOCK_EXCLUSIVE.equals(stockMode)
                    && !BizConstants.LOTTERY_STOCK_PUBLIC.equals(stockMode))
            {
                throw new ServiceException("第" + (i + 1) + "条策略扣库存方式无效");
            }
        }
    }

    private void fillStrategyDefaults(BizLotteryWinStrategy strategy, Long activityId, String operator)
    {
        strategy.setActivityId(activityId);
        strategy.setCreateBy(operator);
        if (StringUtils.isEmpty(strategy.getStatus()))
        {
            strategy.setStatus(BizConstants.STATUS_OK);
        }
        if (StringUtils.isEmpty(strategy.getLoopMode()))
        {
            strategy.setLoopMode(BizConstants.LOTTERY_LOOP_ONCE);
        }
        if (StringUtils.isEmpty(strategy.getStockMode()))
        {
            strategy.setStockMode(BizConstants.LOTTERY_STOCK_AUTO);
        }
        if (strategy.getTargetType() != null && strategy.getTargetType().intValue() == BizConstants.LOTTERY_TARGET_ALL)
        {
            strategy.setUserIds(null);
            strategy.setPackageId(null);
        }
        else if (strategy.getTargetType() != null && strategy.getTargetType().intValue() == BizConstants.LOTTERY_TARGET_USER)
        {
            strategy.setPackageId(null);
        }
        else if (strategy.getTargetType() != null && strategy.getTargetType().intValue() == BizConstants.LOTTERY_TARGET_CROWD)
        {
            strategy.setUserIds(null);
        }
    }
}
