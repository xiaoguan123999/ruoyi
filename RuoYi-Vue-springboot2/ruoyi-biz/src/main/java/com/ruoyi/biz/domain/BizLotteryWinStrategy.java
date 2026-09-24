package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 大转盘步进必中策略
 */
public class BizLotteryWinStrategy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long strategyId;
    private Long activityId;
    /** 1全员 2指定用户 3人群包 */
    private Integer targetType;
    private String userIds;
    private Long packageId;
    private Long targetPrizeId;
    /** 第X次 / 每X次 */
    private Integer triggerCount;
    /** ONCE仅一次 LOOP每N次循环 */
    private String loopMode;
    /** EXCLUSIVE仅专属 PUBLIC仅公海 AUTO专属优先 */
    private String stockMode;
    private String status;
    private transient String packageName;

    public Long getStrategyId() { return strategyId; }
    public void setStrategyId(Long strategyId) { this.strategyId = strategyId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public String getUserIds() { return userIds; }
    public void setUserIds(String userIds) { this.userIds = userIds; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public Long getTargetPrizeId() { return targetPrizeId; }
    public void setTargetPrizeId(Long targetPrizeId) { this.targetPrizeId = targetPrizeId; }
    public Integer getTriggerCount() { return triggerCount; }
    public void setTriggerCount(Integer triggerCount) { this.triggerCount = triggerCount; }
    public String getLoopMode() { return loopMode; }
    public void setLoopMode(String loopMode) { this.loopMode = loopMode; }
    public String getStockMode() { return stockMode; }
    public void setStockMode(String stockMode) { this.stockMode = stockMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
}
