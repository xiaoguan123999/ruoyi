package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizLotteryActivity;
import com.ruoyi.biz.domain.BizLotteryDrawInfo;
import com.ruoyi.biz.domain.BizLotteryDrawResult;
import com.ruoyi.biz.domain.BizLotteryMember;
import com.ruoyi.biz.domain.BizLotteryPrize;
import com.ruoyi.biz.domain.BizLotteryPrizeView;
import com.ruoyi.biz.domain.BizLotteryRecord;
import com.ruoyi.biz.domain.BizLotteryWinStrategy;
import com.ruoyi.biz.lottery.CrowdRuleMatcher;
import com.ruoyi.biz.lottery.LotteryRedisKeys;
import com.ruoyi.biz.mapper.BizLotteryActivityMapper;
import com.ruoyi.biz.mapper.BizLotteryMemberMapper;
import com.ruoyi.biz.mapper.BizLotteryPrizeMapper;
import com.ruoyi.biz.mapper.BizLotteryRecordMapper;
import com.ruoyi.biz.mapper.BizLotteryWinStrategyMapper;
import com.ruoyi.biz.service.IBizLotteryChanceService;
import com.ruoyi.biz.service.IBizLotteryDrawService;
import com.ruoyi.biz.service.IBizLotteryStockService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 大转盘抽奖引擎：频控 → 步进必中分流 → Redis Lua 原子扣库存 → 流水/入账
 */
@Service
public class BizLotteryDrawServiceImpl implements IBizLotteryDrawService
{
    private static final Logger log = LoggerFactory.getLogger(BizLotteryDrawServiceImpl.class);

    @Autowired
    private BizLotteryActivityMapper lotteryActivityMapper;

    @Autowired
    private BizLotteryPrizeMapper lotteryPrizeMapper;

    @Autowired
    private BizLotteryWinStrategyMapper lotteryWinStrategyMapper;

    @Autowired
    private BizLotteryRecordMapper lotteryRecordMapper;

    @Autowired
    private BizLotteryMemberMapper lotteryMemberMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisScript<String> lotteryDrawScript;

    @Autowired
    private IBizLotteryStockService lotteryStockService;

    @Autowired
    private CrowdRuleMatcher crowdRuleMatcher;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizLotteryChanceService lotteryChanceService;

    @Override
    public BizLotteryDrawInfo getCurrentInfo(Long memberId)
    {
        BizLotteryActivity activity = requireActiveActivity();
        Long activityId = activity.getActivityId();
        BizLotteryDrawInfo info = new BizLotteryDrawInfo();
        info.setActivityId(activityId);
        info.setTitle(activity.getTitle());
        info.setStartTime(activity.getStartTime());
        info.setEndTime(activity.getEndTime());
        info.setIntervalHours(activity.getIntervalHours());
        info.setRuleText(activity.getRuleText());

        lotteryChanceService.tryGrantByRules(activityId, memberId);
        int chanceBalance = lotteryChanceService.getBalance(activityId, memberId);
        info.setChanceBalance(Integer.valueOf(chanceBalance));

        int drawCount = readDrawCount(activityId, memberId);
        info.setDrawCount(Integer.valueOf(drawCount));

        int intervalHours = activity.getIntervalHours() != null ? activity.getIntervalHours().intValue() : 72;
        if (intervalHours < 0)
        {
            intervalHours = 72;
        }

        String lockKey = LotteryRedisKeys.limit(activityId, memberId);
        Boolean locked = intervalHours > 0 ? stringRedisTemplate.hasKey(lockKey) : Boolean.FALSE;
        boolean canDraw = chanceBalance > 0 && !Boolean.TRUE.equals(locked);
        info.setCanDraw(Boolean.valueOf(canDraw));
        if (!canDraw && intervalHours > 0)
        {
            Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            if (ttl != null && ttl.longValue() > 0L)
            {
                info.setNextDrawTime(new Date(System.currentTimeMillis() + ttl.longValue() * 1000L));
            }
        }

        List<BizLotteryPrize> prizes = lotteryPrizeMapper.selectPrizeListByActivityId(activityId);
        List<BizLotteryPrizeView> views = new ArrayList<BizLotteryPrizeView>();
        if (prizes != null)
        {
            for (int i = 0; i < prizes.size(); i++)
            {
                BizLotteryPrize p = prizes.get(i);
                if (p == null)
                {
                    continue;
                }
                if (StringUtils.isNotEmpty(p.getStatus()) && !"0".equals(p.getStatus()))
                {
                    continue;
                }
                BizLotteryPrizeView v = new BizLotteryPrizeView();
                v.setPrizeId(p.getPrizeId());
                v.setPrizeName(p.getPrizeName());
                v.setPrizeDesc(p.getPrizeDesc());
                v.setImageUrl(p.getImageUrl());
                v.setSectorBgColor(p.getSectorBgColor());
                v.setNameColor(p.getNameColor());
                v.setDescColor(p.getDescColor());
                v.setPrizeType(p.getPrizeType());
                v.setPosition(p.getPosition());
                v.setIsFallback(p.getIsFallback());
                views.add(v);
            }
        }
        info.setPrizes(views);
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizLotteryDrawResult draw(Long memberId)
    {
        if (memberId == null)
        {
            throw new ServiceException("请先登录");
        }
        BizLotteryActivity activity = requireActiveActivity();
        Long activityId = activity.getActivityId();
        // intervalHours=0 表示关闭频控（本地测试用）；null 才默认 72
        int intervalHours = activity.getIntervalHours() != null ? activity.getIntervalHours().intValue() : 72;
        if (intervalHours < 0)
        {
            intervalHours = 72;
        }

        String lockKey = LotteryRedisKeys.limit(activityId, memberId);
        if (intervalHours > 0 && Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey)))
        {
            throw new ServiceException("处于抽奖冷冻期内");
        }

        lotteryChanceService.tryGrantByRules(activityId, memberId);

        String counterKey = LotteryRedisKeys.drawCount(activityId, memberId);
        syncDrawCounter(activityId, memberId, counterKey);
        Long currentDrawCount = stringRedisTemplate.opsForValue().increment(counterKey);
        if (currentDrawCount == null)
        {
            throw new ServiceException("抽奖计数失败，请稍后重试");
        }

        String chanceBizNo = "DRAW_CONSUME:" + activityId + ":" + memberId + ":" + currentDrawCount
                + ":" + IdUtils.fastSimpleUUID();
        if (!lotteryChanceService.consumeForDraw(activityId, memberId, chanceBizNo))
        {
            stringRedisTemplate.opsForValue().decrement(counterKey);
            throw new ServiceException("暂无抽奖次数，请先完成获次条件");
        }

        List<BizLotteryPrize> prizes = lotteryPrizeMapper.selectPrizeListByActivityId(activityId);
        if (prizes == null || prizes.isEmpty())
        {
            stringRedisTemplate.opsForValue().decrement(counterKey);
            lotteryChanceService.refundForDraw(activityId, memberId, chanceBizNo);
            throw new ServiceException("活动奖品未配置");
        }

        boolean isExclusive = false;
        boolean isStrategy = false;
        String stockMode = BizConstants.LOTTERY_STOCK_PUBLIC;
        BizLotteryWinStrategy matched = matchStrategy(activityId, memberId, currentDrawCount.intValue());
        Long targetPrizeId;
        if (matched != null && matched.getTargetPrizeId() != null)
        {
            isStrategy = true;
            targetPrizeId = matched.getTargetPrizeId();
            stockMode = normalizeStockMode(matched.getStockMode());
        }
        else
        {
            targetPrizeId = rollPublicPrizeId(prizes);
            stockMode = BizConstants.LOTTERY_STOCK_PUBLIC;
        }

        lotteryStockService.ensureActivityStock(activityId);

        Long fallbackPrizeId = findFallbackPrizeId(prizes);
        String publicKey = LotteryRedisKeys.publicStock(activityId);
        String exclusiveKey = LotteryRedisKeys.exclusiveStock(activityId);
        String fallbackArg = fallbackPrizeId == null ? "" : String.valueOf(fallbackPrizeId);
        String luaResult = stringRedisTemplate.execute(
                lotteryDrawScript,
                Arrays.asList(publicKey, exclusiveKey),
                String.valueOf(targetPrizeId),
                stockMode,
                fallbackArg);

        // 库存脏数据：热更后再试
        if ("-2".equals(luaResult))
        {
            lotteryStockService.warmActivityStock(activityId);
            luaResult = stringRedisTemplate.execute(
                    lotteryDrawScript,
                    Arrays.asList(publicKey, exclusiveKey),
                    String.valueOf(targetPrizeId),
                    stockMode,
                    fallbackArg);
        }
        if ("-2".equals(luaResult) && fallbackPrizeId != null)
        {
            stringRedisTemplate.opsForHash().put(publicKey, String.valueOf(fallbackPrizeId), "-1");
            luaResult = stringRedisTemplate.execute(
                    lotteryDrawScript,
                    Arrays.asList(publicKey, exclusiveKey),
                    String.valueOf(fallbackPrizeId),
                    BizConstants.LOTTERY_STOCK_PUBLIC,
                    String.valueOf(fallbackPrizeId));
            log.warn("大转盘目标库存不足，已强制走兜底奖 activity={} member={} target={} fallback={} result={}",
                    activityId, memberId, targetPrizeId, fallbackPrizeId, luaResult);
        }

        if ("-2".equals(luaResult))
        {
            stringRedisTemplate.opsForValue().decrement(counterKey);
            lotteryChanceService.refundForDraw(activityId, memberId, chanceBizNo);
            log.error("大转盘库存熔断(含兜底失败) activity={} member={} prize={} fallback={} drawCount={} stockMode={}",
                    activityId, memberId, targetPrizeId, fallbackPrizeId, currentDrawCount, stockMode);
            BizLotteryDrawResult melt = new BizLotteryDrawResult();
            melt.setIsMeltdown(Boolean.TRUE);
            melt.setIsStrategy(Boolean.valueOf(isStrategy));
            melt.setMsg("大奖过于火爆，正在紧急打包中");
            return melt;
        }

        String poolFlag = "P";
        String prizeToken = luaResult;
        int sep = luaResult == null ? -1 : luaResult.indexOf('|');
        if (sep > 0)
        {
            poolFlag = luaResult.substring(0, sep);
            prizeToken = luaResult.substring(sep + 1);
        }

        Long wonPrizeId;
        try
        {
            wonPrizeId = Long.valueOf(prizeToken);
        }
        catch (Exception e)
        {
            stringRedisTemplate.opsForValue().decrement(counterKey);
            lotteryChanceService.refundForDraw(activityId, memberId, chanceBizNo);
            throw new ServiceException("抽奖结果异常，请稍后重试");
        }

        BizLotteryPrize won = lotteryPrizeMapper.selectPrizeById(wonPrizeId);
        if (won == null)
        {
            stringRedisTemplate.opsForValue().decrement(counterKey);
            lotteryChanceService.refundForDraw(activityId, memberId, chanceBizNo);
            throw new ServiceException("奖品不存在");
        }

        boolean usedFallback = "F".equals(poolFlag)
                || (fallbackPrizeId != null && fallbackPrizeId.equals(wonPrizeId) && !fallbackPrizeId.equals(targetPrizeId));
        isExclusive = "E".equals(poolFlag);
        if (usedFallback)
        {
            log.info("大转盘目标库存不足，发放兜底奖 activity={} member={} target={} fallback={}",
                    activityId, memberId, targetPrizeId, fallbackPrizeId);
        }
        else if (isStrategy && "P".equals(poolFlag))
        {
            log.info("大转盘必中已扣公海库存 activity={} member={} prize={} stockMode={}",
                    activityId, memberId, targetPrizeId, stockMode);
        }
        else if (isStrategy && "E".equals(poolFlag))
        {
            log.info("大转盘必中已扣专属库存 activity={} member={} prize={} stockMode={}",
                    activityId, memberId, targetPrizeId, stockMode);
        }

        if (intervalHours > 0)
        {
            stringRedisTemplate.opsForValue().set(lockKey, "1", intervalHours, TimeUnit.HOURS);
        }

        String grantStatus = BizConstants.LOTTERY_GRANT_CLAIM;
        if (won.getPrizeType() != null && won.getPrizeType().intValue() == BizConstants.LOTTERY_PRIZE_VIRTUAL)
        {
            BigDecimal amount = won.getAssistAmount() != null ? won.getAssistAmount() : BigDecimal.ZERO;
            String currency = StringUtils.isNotEmpty(won.getCurrency()) ? won.getCurrency() : BizConstants.CURRENCY_CNY;
            if (amount.compareTo(BigDecimal.ZERO) > 0)
            {
                walletService.credit(memberId, currency, amount, BizConstants.BIZ_LOTTERY_ASSIST,
                        wonPrizeId, "大转盘助力值:" + won.getPrizeName(), BizConstants.WALLET_ASSIST);
            }
            grantStatus = BizConstants.LOTTERY_GRANT_CREDITED;
        }

        BizLotteryRecord record = new BizLotteryRecord();
        record.setActivityId(activityId);
        record.setMemberId(memberId);
        record.setDrawCount(Integer.valueOf(currentDrawCount.intValue()));
        record.setPrizeId(wonPrizeId);
        record.setPrizeName(won.getPrizeName());
        record.setPrizeType(won.getPrizeType());
        record.setPoolType(isExclusive ? BizConstants.LOTTERY_POOL_EXCLUSIVE : BizConstants.LOTTERY_POOL_PUBLIC);
        record.setIsStrategy(isStrategy ? "1" : "0");
        record.setIsMeltdown("0");
        record.setGrantStatus(grantStatus);
        record.setBizNo("LOT-" + activityId + "-" + memberId + "-" + currentDrawCount + "-" + IdUtils.fastSimpleUUID());
        if (usedFallback)
        {
            record.setRemark("目标库存不足，发放兜底奖");
        }
        lotteryRecordMapper.insertRecord(record);

        BizLotteryMember memberRow = new BizLotteryMember();
        memberRow.setActivityId(activityId);
        memberRow.setMemberId(memberId);
        memberRow.setDrawCount(Integer.valueOf(currentDrawCount.intValue()));
        memberRow.setLastDrawTime(new Date());
        lotteryMemberMapper.upsertLotteryMember(memberRow);

        BizLotteryDrawResult result = new BizLotteryDrawResult();
        result.setPrizeId(wonPrizeId);
        result.setPrizeName(won.getPrizeName());
        result.setPrizeType(won.getPrizeType());
        result.setPosition(won.getPosition());
        result.setIsMeltdown(Boolean.FALSE);
        result.setIsStrategy(Boolean.valueOf(isStrategy));
        result.setMsg("恭喜获得：" + won.getPrizeName());
        return result;
    }

    private BizLotteryActivity requireActiveActivity()
    {
        BizLotteryActivity activity = lotteryActivityMapper.selectCurrentActiveActivity();
        if (activity == null)
        {
            throw new ServiceException("暂无进行中的抽奖活动");
        }
        return activity;
    }

    private int readDrawCount(Long activityId, Long memberId)
    {
        String val = stringRedisTemplate.opsForValue().get(LotteryRedisKeys.drawCount(activityId, memberId));
        if (StringUtils.isNotEmpty(val))
        {
            try
            {
                return Integer.parseInt(val);
            }
            catch (NumberFormatException ignored)
            {
            }
        }
        return resolvePersistedDrawCount(activityId, memberId);
    }

    /**
     * 将 Redis 计数对齐到流水/会员表最大值，避免手工改次数或 Redis 回退导致 uk_lottery_record_draw 冲突。
     */
    private void syncDrawCounter(Long activityId, Long memberId, String counterKey)
    {
        int floor = resolvePersistedDrawCount(activityId, memberId);
        String val = stringRedisTemplate.opsForValue().get(counterKey);
        int current = 0;
        if (StringUtils.isNotEmpty(val))
        {
            try
            {
                current = Integer.parseInt(val);
            }
            catch (NumberFormatException ignored)
            {
                current = 0;
            }
        }
        if (current < floor)
        {
            stringRedisTemplate.opsForValue().set(counterKey, String.valueOf(floor));
            log.warn("大转盘抽奖计数已对齐 activity={} member={} redis={} floor={}",
                    activityId, memberId, Integer.valueOf(current), Integer.valueOf(floor));
        }
    }

    private int resolvePersistedDrawCount(Long activityId, Long memberId)
    {
        int max = 0;
        Integer fromRecord = lotteryRecordMapper.selectMaxDrawCount(activityId, memberId);
        if (fromRecord != null && fromRecord.intValue() > max)
        {
            max = fromRecord.intValue();
        }
        BizLotteryMember row = lotteryMemberMapper.selectLotteryMember(activityId, memberId);
        if (row != null && row.getDrawCount() != null && row.getDrawCount().intValue() > max)
        {
            max = row.getDrawCount().intValue();
        }
        return max;
    }

    private BizLotteryWinStrategy matchStrategy(Long activityId, Long memberId, int drawCount)
    {
        List<BizLotteryWinStrategy> strategies = lotteryWinStrategyMapper.selectStrategyListByActivityId(activityId);
        if (strategies == null || strategies.isEmpty() || drawCount < 1)
        {
            return null;
        }
        // 仅一次优先于循环，避免第50次被「每10次」抢先
        BizLotteryWinStrategy onceHit = findMatchingStrategy(strategies, memberId, drawCount, true);
        if (onceHit != null)
        {
            return onceHit;
        }
        return findMatchingStrategy(strategies, memberId, drawCount, false);
    }

    private BizLotteryWinStrategy findMatchingStrategy(List<BizLotteryWinStrategy> strategies, Long memberId,
            int drawCount, boolean onceOnly)
    {
        for (int i = 0; i < strategies.size(); i++)
        {
            BizLotteryWinStrategy s = strategies.get(i);
            if (s == null || (StringUtils.isNotEmpty(s.getStatus()) && !"0".equals(s.getStatus())))
            {
                continue;
            }
            if (s.getTriggerCount() == null || s.getTriggerCount().intValue() < 1
                    || s.getTargetType() == null || s.getTargetPrizeId() == null)
            {
                continue;
            }
            boolean isLoop = BizConstants.LOTTERY_LOOP_CYCLE.equals(normalizeLoopMode(s.getLoopMode()));
            if (onceOnly == isLoop)
            {
                continue;
            }
            int n = s.getTriggerCount().intValue();
            if (onceOnly)
            {
                if (drawCount != n)
                {
                    continue;
                }
            }
            else if (drawCount < n || drawCount % n != 0)
            {
                continue;
            }
            if (audienceMatched(s, memberId))
            {
                return s;
            }
        }
        return null;
    }

    private boolean audienceMatched(BizLotteryWinStrategy s, Long memberId)
    {
        int type = s.getTargetType().intValue();
        if (type == BizConstants.LOTTERY_TARGET_ALL)
        {
            return true;
        }
        if (type == BizConstants.LOTTERY_TARGET_USER)
        {
            return containsMemberId(s.getUserIds(), memberId);
        }
        if (type == BizConstants.LOTTERY_TARGET_CROWD)
        {
            return s.getPackageId() != null && crowdRuleMatcher.match(s.getPackageId(), memberId);
        }
        return false;
    }

    private String normalizeLoopMode(String loopMode)
    {
        if (BizConstants.LOTTERY_LOOP_CYCLE.equals(loopMode))
        {
            return BizConstants.LOTTERY_LOOP_CYCLE;
        }
        return BizConstants.LOTTERY_LOOP_ONCE;
    }

    private String normalizeStockMode(String stockMode)
    {
        if (BizConstants.LOTTERY_STOCK_EXCLUSIVE.equals(stockMode)
                || BizConstants.LOTTERY_STOCK_PUBLIC.equals(stockMode)
                || BizConstants.LOTTERY_STOCK_AUTO.equals(stockMode))
        {
            return stockMode;
        }
        return BizConstants.LOTTERY_STOCK_AUTO;
    }

    private boolean containsMemberId(String userIds, Long memberId)
    {
        if (StringUtils.isEmpty(userIds) || memberId == null)
        {
            return false;
        }
        String[] parts = userIds.split("[,，\\s]+");
        String id = String.valueOf(memberId);
        for (int i = 0; i < parts.length; i++)
        {
            if (id.equals(parts[i].trim()))
            {
                return true;
            }
        }
        return false;
    }

    private Long findFallbackPrizeId(List<BizLotteryPrize> prizes)
    {
        if (prizes == null)
        {
            return null;
        }
        for (int i = 0; i < prizes.size(); i++)
        {
            BizLotteryPrize p = prizes.get(i);
            if (p == null || p.getPrizeId() == null)
            {
                continue;
            }
            if (StringUtils.isNotEmpty(p.getStatus()) && !"0".equals(p.getStatus()))
            {
                continue;
            }
            if ("1".equals(p.getIsFallback()))
            {
                return p.getPrizeId();
            }
        }
        return null;
    }

    private Long rollPublicPrizeId(List<BizLotteryPrize> prizes)
    {
        int total = 0;
        List<BizLotteryPrize> pool = new ArrayList<BizLotteryPrize>();
        for (int i = 0; i < prizes.size(); i++)
        {
            BizLotteryPrize p = prizes.get(i);
            if (p == null || p.getPrizeId() == null)
            {
                continue;
            }
            if (StringUtils.isNotEmpty(p.getStatus()) && !"0".equals(p.getStatus()))
            {
                continue;
            }
            int prob = p.getProbability() != null ? p.getProbability().intValue() : 0;
            if (prob <= 0)
            {
                continue;
            }
            total += prob;
            pool.add(p);
        }
        if (total <= 0 || pool.isEmpty())
        {
            throw new ServiceException("活动奖品概率未配置");
        }
        int hit = ThreadLocalRandom.current().nextInt(total);
        int acc = 0;
        for (int i = 0; i < pool.size(); i++)
        {
            BizLotteryPrize p = pool.get(i);
            acc += p.getProbability().intValue();
            if (hit < acc)
            {
                return p.getPrizeId();
            }
        }
        return pool.get(pool.size() - 1).getPrizeId();
    }
}
