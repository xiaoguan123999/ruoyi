package com.ruoyi.biz.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.domain.BizLotteryPrize;
import com.ruoyi.biz.lottery.LotteryRedisKeys;
import com.ruoyi.biz.mapper.BizLotteryPrizeMapper;
import com.ruoyi.biz.service.IBizLotteryStockService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class BizLotteryStockServiceImpl implements IBizLotteryStockService
{
    private static final Logger log = LoggerFactory.getLogger(BizLotteryStockServiceImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BizLotteryPrizeMapper lotteryPrizeMapper;

    @Override
    public void warmActivityStock(Long activityId)
    {
        if (activityId == null)
        {
            throw new ServiceException("活动ID不能为空");
        }
        List<BizLotteryPrize> prizes = lotteryPrizeMapper.selectPrizeListByActivityId(activityId);
        String publicKey = LotteryRedisKeys.publicStock(activityId);
        String exclusiveKey = LotteryRedisKeys.exclusiveStock(activityId);
        stringRedisTemplate.delete(publicKey);
        stringRedisTemplate.delete(exclusiveKey);
        if (prizes == null || prizes.isEmpty())
        {
            log.warn("活动{}无奖品，跳过库存预热", activityId);
            return;
        }
        Map<String, String> publicMap = new HashMap<String, String>();
        Map<String, String> exclusiveMap = new HashMap<String, String>();
        for (int i = 0; i < prizes.size(); i++)
        {
            BizLotteryPrize prize = prizes.get(i);
            if (prize.getPrizeId() == null)
            {
                continue;
            }
            String field = String.valueOf(prize.getPrizeId());
            int publicStock = prize.getPublicStock() != null ? prize.getPublicStock().intValue() : 0;
            int exclusiveStock = prize.getExclusiveStock() != null ? prize.getExclusiveStock().intValue() : 0;
            // 兜底奖必须无限库存，避免批量失败触发熔断
            if ("1".equals(prize.getIsFallback()))
            {
                if (publicStock != -1)
                {
                    log.warn("活动{}兜底奖{}普通库存={}，预热强制写成-1", activityId, prize.getPrizeId(), Integer.valueOf(publicStock));
                    publicStock = -1;
                }
                if (exclusiveStock != -1)
                {
                    exclusiveStock = -1;
                }
            }
            publicMap.put(field, String.valueOf(publicStock));
            exclusiveMap.put(field, String.valueOf(exclusiveStock));
        }
        if (!publicMap.isEmpty())
        {
            stringRedisTemplate.opsForHash().putAll(publicKey, publicMap);
            stringRedisTemplate.opsForHash().putAll(exclusiveKey, exclusiveMap);
        }
        log.info("活动{}库存已预热，奖品数={}", activityId, Integer.valueOf(publicMap.size()));
    }

    @Override
    public void ensureActivityStock(Long activityId)
    {
        String publicKey = LotteryRedisKeys.publicStock(activityId);
        Boolean exists = stringRedisTemplate.hasKey(publicKey);
        if (!Boolean.TRUE.equals(exists))
        {
            warmActivityStock(activityId);
            return;
        }
        // 已有 Hash 时也校正兜底奖字段，防止历史脏数据(0)导致熔断
        List<BizLotteryPrize> prizes = lotteryPrizeMapper.selectPrizeListByActivityId(activityId);
        if (prizes == null)
        {
            return;
        }
        for (int i = 0; i < prizes.size(); i++)
        {
            BizLotteryPrize prize = prizes.get(i);
            if (prize == null || prize.getPrizeId() == null || !"1".equals(prize.getIsFallback()))
            {
                continue;
            }
            String field = String.valueOf(prize.getPrizeId());
            Object cur = stringRedisTemplate.opsForHash().get(publicKey, field);
            if (cur == null || !"-1".equals(String.valueOf(cur)))
            {
                stringRedisTemplate.opsForHash().put(publicKey, field, "-1");
                stringRedisTemplate.opsForHash().put(LotteryRedisKeys.exclusiveStock(activityId), field, "-1");
                log.warn("活动{}兜底奖{} Redis库存已校正为-1(原值={})", activityId, prize.getPrizeId(), cur);
            }
        }
    }
}
