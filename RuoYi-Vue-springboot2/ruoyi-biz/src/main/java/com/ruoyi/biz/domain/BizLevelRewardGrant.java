package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("等级奖励发放")
public class BizLevelRewardGrant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("发放ID")
    private Long grantId;
    @ApiModelProperty("会员ID")
    private Long memberId;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("等级ID")
    private Long levelId;
    @ApiModelProperty("等级名称")
    private String levelName;
    @ApiModelProperty("去重键")
    private String cycleKey;
    @ApiModelProperty("ONCE/MONTHLY/PERMANENT")
    private String grantCycle;
    @ApiModelProperty("AUTO/MANUAL")
    private String grantMode;
    @ApiModelProperty("发放币种")
    private String currency;
    @ApiModelProperty("金额")
    private BigDecimal amount;
    @ApiModelProperty("0待发放 1已发放 2已拒绝")
    private String status;
    @ApiModelProperty("发放人")
    private String payBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    public Long getGrantId() { return grantId; }
    public void setGrantId(Long grantId) { this.grantId = grantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public String getCycleKey() { return cycleKey; }
    public void setCycleKey(String cycleKey) { this.cycleKey = cycleKey; }
    public String getGrantCycle() { return grantCycle; }
    public void setGrantCycle(String grantCycle) { this.grantCycle = grantCycle; }
    public String getGrantMode() { return grantMode; }
    public void setGrantMode(String grantMode) { this.grantMode = grantMode; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayBy() { return payBy; }
    public void setPayBy(String payBy) { this.payBy = payBy; }
    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }
}
