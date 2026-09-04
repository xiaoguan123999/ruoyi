package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.common.annotation.Excel;
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
    @Excel(name = "会员ID")
    @ApiModelProperty("会员ID/邀请码")
    private Long memberId;

    /** 手机号 */
    @Excel(name = "手机号")
    @ApiModelProperty("手机号")
    private String phone;

    /** 密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 支付/交易密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String payPassword;

    /** 邀请码 */
    @Excel(name = "邀请码")
    @ApiModelProperty("邀请码")
    private String inviteCode;

    /** 上级ID */
    @Excel(name = "直推上级ID")
    @ApiModelProperty("上级ID")
    private Long parentId;

    @Excel(name = "直推上级邀请码")
    @ApiModelProperty("直推上级邀请码")
    private String parentInviteCode;

    /** 祖级列表 */
    @ApiModelProperty("祖级列表")
    private String ancestors;

    /** 真实姓名 */
    @Excel(name = "姓名")
    @ApiModelProperty("真实姓名")
    private String realName;

    /** 身份证号 */
    @ApiModelProperty("身份证号")
    private String idCard;

    /** 实名状态 */
    @Excel(name = "实名", readConverterExp = "0=未实名,1=已实名")
    @ApiModelProperty("实名状态：0未实名 1已实名")
    private String kycStatus;

    /** 等级ID，空表示无等级 */
    @ApiModelProperty("等级ID，空表示无等级")
    private Long levelId;

    /** 等级名称 */
    @Excel(name = "等级")
    @ApiModelProperty("等级名称")
    private String levelName;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @ApiModelProperty("账号状态：0正常 1停用")
    private String status;

    @Excel(name = "提现状态", readConverterExp = "0=正常,1=禁止")
    @ApiModelProperty("提现状态：0正常 1禁止")
    private String withdrawStatus;

    @Excel(name = "测试账号", readConverterExp = "0=正式,1=测试")
    @ApiModelProperty("测试账号：0否 1是。测试用户可正常使用，数据不计入任何统计")
    private String testFlag;

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
    @Excel(name = "团队人数")
    @ApiModelProperty("团队人数")
    private Integer teamCount;

    @Excel(name = "直推人数")
    @ApiModelProperty("直推人数")
    private Integer directCount;

    @ApiModelProperty("筛选：直推人数不少于")
    private Integer minDirectCount;

    @ApiModelProperty("筛选：直推人数不多于")
    private Integer maxDirectCount;

    @ApiModelProperty("勾选导出的会员ID")
    private Long[] memberIds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("最后登录时间")
    private Date lastLoginTime;

    @Excel(name = "注册IP")
    @ApiModelProperty("注册IP")
    private String registerIp;

    @Excel(name = "最后登录IP")
    @ApiModelProperty("最后登录IP")
    private String lastLoginIp;

    @Excel(name = "注册时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

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

    public String getParentInviteCode()
    {
        return parentInviteCode;
    }

    public void setParentInviteCode(String parentInviteCode)
    {
        this.parentInviteCode = parentInviteCode;
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

    public String getWithdrawStatus()
    {
        return withdrawStatus;
    }

    public void setWithdrawStatus(String withdrawStatus)
    {
        this.withdrawStatus = withdrawStatus;
    }

    public String getTestFlag()
    {
        return testFlag;
    }

    public void setTestFlag(String testFlag)
    {
        this.testFlag = testFlag;
    }

    @ApiModelProperty("是否测试账号")
    public Boolean getTestFlagFlag()
    {
        return Boolean.valueOf(testAccount());
    }

    public boolean testAccount()
    {
        return "1".equals(testFlag);
    }

    @ApiModelProperty("是否禁止提现")
    public Boolean getWithdrawForbidden()
    {
        return Boolean.valueOf(BizConstants.WITHDRAW_FORBID.equals(withdrawStatus));
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

    public Date getLastLoginTime()
    {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime)
    {
        this.lastLoginTime = lastLoginTime;
    }

    public String getRegisterIp()
    {
        return registerIp;
    }

    public void setRegisterIp(String registerIp)
    {
        this.registerIp = registerIp;
    }

    public String getLastLoginIp()
    {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp)
    {
        this.lastLoginIp = lastLoginIp;
    }

    public Integer getDirectCount()
    {
        return directCount;
    }

    public void setDirectCount(Integer directCount)
    {
        this.directCount = directCount;
    }

    public Integer getMinDirectCount()
    {
        return minDirectCount;
    }

    public void setMinDirectCount(Integer minDirectCount)
    {
        this.minDirectCount = minDirectCount;
    }

    public Integer getMaxDirectCount()
    {
        return maxDirectCount;
    }

    public void setMaxDirectCount(Integer maxDirectCount)
    {
        this.maxDirectCount = maxDirectCount;
    }

    public Long[] getMemberIds()
    {
        return memberIds;
    }

    public void setMemberIds(Long[] memberIds)
    {
        this.memberIds = memberIds;
    }

    public Date getApplyTime()
    {
        return applyTime != null ? applyTime : getCreateTime();
    }

    public void setApplyTime(Date applyTime)
    {
        this.applyTime = applyTime;
    }

}
