package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * C端会员
 */
@ApiModel("会员资料（含资产卡字段）")
public class BizMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会员ID/邀请码 */
    @ApiModelProperty("会员ID/邀请码")
    private Long memberId;

    /** 手机号 */
    @ApiModelProperty("手机号")
    private String phone;

    /** 密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 支付/交易密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String payPassword;

    /** 邀请码 */
    @ApiModelProperty("邀请码")
    private String inviteCode;

    /** 上级ID */
    @ApiModelProperty("上级ID")
    private Long parentId;

    /** 祖级列表 */
    @ApiModelProperty("祖级列表")
    private String ancestors;

    /** 真实姓名 */
    @ApiModelProperty("真实姓名")
    private String realName;

    /** 身份证号 */
    @ApiModelProperty("身份证号")
    private String idCard;

    /** 实名状态 */
    @ApiModelProperty("实名状态：0未实名 1已实名")
    private String kycStatus;

    /** 等级ID */
    @ApiModelProperty("等级ID")
    private Long levelId;

    /** 等级名称 */
    @ApiModelProperty("等级名称")
    private String levelName;

    /** 状态 */
    @ApiModelProperty("账号状态：0正常 1停用")
    private String status;

    /** 谷歌验证密钥 */
    @JsonIgnore
    private String gaSecret;

    /** 谷歌验证 0未绑定 1已绑定 */
    @ApiModelProperty("谷歌验证 0未绑定 1已绑定")
    private String gaStatus;

    @ApiModelProperty("是否已设置支付密码")
    private Boolean hasPayPassword;

    /** 人民币可用 */
    @ApiModelProperty("人民币可用")
    private BigDecimal cnyAvailable;

    /** 人民币冻结 */
    @ApiModelProperty("人民币冻结")
    private BigDecimal cnyFrozen;

    /** USDT可用 */
    @ApiModelProperty("USDT可用")
    private BigDecimal usdtAvailable;

    /** USDT冻结 */
    @ApiModelProperty("USDT冻结")
    private BigDecimal usdtFrozen;

    /** 人民币累计产品收益 */
    @ApiModelProperty("人民币累计产品收益")
    private BigDecimal cnyProductIncome;

    /** USDT累计产品收益 */
    @ApiModelProperty("USDT累计产品收益")
    private BigDecimal usdtProductIncome;

    /** 人民币推广收益（兼容 App 助力值字段） */
    @ApiModelProperty("人民币推广收益累计（签到、实名奖励、邀请、分佣、等级奖励）")
    private BigDecimal cnyAssistValue;

    /** USDT推广收益（兼容 App 助力值字段） */
    @ApiModelProperty("USDT推广收益累计（签到、实名奖励、邀请、分佣、等级奖励）")
    private BigDecimal usdtAssistValue;

    /** 团队人数 */
    @ApiModelProperty("团队人数")
    private Integer teamCount;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPayPassword()
    {
        return payPassword;
    }

    public void setPayPassword(String payPassword)
    {
        this.payPassword = payPassword;
    }

    public Boolean getHasPayPassword()
    {
        if (hasPayPassword != null)
        {
            return hasPayPassword;
        }
        return payPassword != null && payPassword.length() > 0;
    }

    public void setHasPayPassword(Boolean hasPayPassword)
    {
        this.hasPayPassword = hasPayPassword;
    }

    public String getInviteCode()
    {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getKycStatus()
    {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus)
    {
        this.kycStatus = kycStatus;
    }

    public Long getLevelId()
    {
        return levelId;
    }

    public void setLevelId(Long levelId)
    {
        this.levelId = levelId;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public void setLevelName(String levelName)
    {
        this.levelName = levelName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getGaSecret()
    {
        return gaSecret;
    }

    public void setGaSecret(String gaSecret)
    {
        this.gaSecret = gaSecret;
    }

    public String getGaStatus()
    {
        return gaStatus;
    }

    public void setGaStatus(String gaStatus)
    {
        this.gaStatus = gaStatus;
    }

    public BigDecimal getCnyAvailable()
    {
        return cnyAvailable;
    }

    public void setCnyAvailable(BigDecimal cnyAvailable)
    {
        this.cnyAvailable = cnyAvailable;
    }

    public BigDecimal getCnyFrozen()
    {
        return cnyFrozen;
    }

    public void setCnyFrozen(BigDecimal cnyFrozen)
    {
        this.cnyFrozen = cnyFrozen;
    }

    public BigDecimal getUsdtAvailable()
    {
        return usdtAvailable;
    }

    public void setUsdtAvailable(BigDecimal usdtAvailable)
    {
        this.usdtAvailable = usdtAvailable;
    }

    public BigDecimal getUsdtFrozen()
    {
        return usdtFrozen;
    }

    public void setUsdtFrozen(BigDecimal usdtFrozen)
    {
        this.usdtFrozen = usdtFrozen;
    }

    public BigDecimal getCnyProductIncome()
    {
        return cnyProductIncome;
    }

    public void setCnyProductIncome(BigDecimal cnyProductIncome)
    {
        this.cnyProductIncome = cnyProductIncome;
    }

    public BigDecimal getUsdtProductIncome()
    {
        return usdtProductIncome;
    }

    public void setUsdtProductIncome(BigDecimal usdtProductIncome)
    {
        this.usdtProductIncome = usdtProductIncome;
    }

    public BigDecimal getCnyAssistValue()
    {
        return cnyAssistValue;
    }

    public void setCnyAssistValue(BigDecimal cnyAssistValue)
    {
        this.cnyAssistValue = cnyAssistValue;
    }

    public BigDecimal getUsdtAssistValue()
    {
        return usdtAssistValue;
    }

    public void setUsdtAssistValue(BigDecimal usdtAssistValue)
    {
        this.usdtAssistValue = usdtAssistValue;
    }

    public Integer getTeamCount()
    {
        return teamCount;
    }

    public void setTeamCount(Integer teamCount)
    {
        this.teamCount = teamCount;
    }

}
