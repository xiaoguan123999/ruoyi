package com.ruoyi.biz.service;

/**
 * 大转盘库存预热（Redis）
 */
public interface IBizLotteryStockService
{
    /**
     * 将活动奖品公海/专属库存写入 Redis Hash（String 值）
     */
    void warmActivityStock(Long activityId);

    /**
     * 若 Redis 公海库存 Key 不存在则预热
     */
    void ensureActivityStock(Long activityId);
}
