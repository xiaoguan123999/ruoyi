package com.ruoyi.biz.lottery;

/**
 * 大转盘 Redis Key 工厂
 */
public final class LotteryRedisKeys
{
    private LotteryRedisKeys()
    {
    }

    /** 普通库存 Hash */
    public static String publicStock(Long activityId)
    {
        return "lottery:prizes:public:stock:" + activityId;
    }

    /** 必中库存 Hash */
    public static String exclusiveStock(Long activityId)
    {
        return "lottery:prizes:exclusive:stock:" + activityId;
    }

    /** 周期频控锁 */
    public static String limit(Long activityId, Long memberId)
    {
        return "lottery:limit:uid:" + activityId + ":" + memberId;
    }

    /** 用户累计抽奖次数 */
    public static String drawCount(Long activityId, Long memberId)
    {
        return "lottery:user:total:draw:" + activityId + ":" + memberId;
    }
}
