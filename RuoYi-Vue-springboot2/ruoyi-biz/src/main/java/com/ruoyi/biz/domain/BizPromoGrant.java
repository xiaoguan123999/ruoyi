package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("实名注册与推广奖励发放")
public class BizPromoGrant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("发放ID")
    private Long grantId;
    @ApiModelProperty("收款会员")
    private Long memberId;
    @ApiModelProperty("收款手机号")
    private String phone;
    @ApiModelProperty("来源会员：自领=本人，推广=被邀请人")
    private Long fromMemberId;
    @ApiModelProperty("来源手机号")
    private String fromPhone;
    @ApiModelProperty("KYC_SELF实名自领 INVITE推广奖励")
    private String grantType;
    @ApiModelProperty("币种")
    private String currency;
    @ApiModelProperty("金额")
    private BigDecimal amount;
    @ApiModelProperty("1已发放")
    private String status;

    public Long getGrantId() { return grantId; }
    public void setGrantId(Long grantId) { this.grantId = grantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }
    public String getFromPhone() { return fromPhone; }
    public void setFromPhone(String fromPhone) { this.fromPhone = fromPhone; }
    public String getGrantType() { return grantType; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
