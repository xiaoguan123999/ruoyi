package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 团队分佣记录
 */
public class BizCommissionLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分佣ID */
    private Long commissionId;

    /** 来源会员 */
    private Long fromMemberId;

    /** 来源手机号 */
    private String fromPhone;

    /** 获得分佣会员 */
    private Long toMemberId;

    /** 获得分佣手机号 */
    private String toPhone;

    /** 层级 */
    private Integer teamLevel;

    /** 币种 */
    private String currency;

    /** 充值本金 */
    private BigDecimal baseAmount;

    /** 比例 */
    private BigDecimal rate;

    /** 分佣金额 */
    private BigDecimal amount;

    /** 充值单ID */
    private Long rechargeId;

    public Long getCommissionId()
    {
        return commissionId;
    }

    public void setCommissionId(Long commissionId)
    {
        this.commissionId = commissionId;
    }

    public Long getFromMemberId()
    {
        return fromMemberId;
    }

    public void setFromMemberId(Long fromMemberId)
    {
        this.fromMemberId = fromMemberId;
    }

    public String getFromPhone()
    {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone)
    {
        this.fromPhone = fromPhone;
    }

    public Long getToMemberId()
    {
        return toMemberId;
    }

    public void setToMemberId(Long toMemberId)
    {
        this.toMemberId = toMemberId;
    }

    public String getToPhone()
    {
        return toPhone;
    }

    public void setToPhone(String toPhone)
    {
        this.toPhone = toPhone;
    }

    public Integer getTeamLevel()
    {
        return teamLevel;
    }

    public void setTeamLevel(Integer teamLevel)
    {
        this.teamLevel = teamLevel;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public BigDecimal getBaseAmount()
    {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount)
    {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getRate()
    {
        return rate;
    }

    public void setRate(BigDecimal rate)
    {
        this.rate = rate;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Long getRechargeId()
    {
        return rechargeId;
    }

    public void setRechargeId(Long rechargeId)
    {
        this.rechargeId = rechargeId;
    }

}
