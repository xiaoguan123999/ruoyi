package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * C端会员
 */
public class BizMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会员ID/邀请码 */
    private Long memberId;

    /** 手机号 */
    private String phone;

    /** 密码 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 邀请码 */
    private String inviteCode;

    /** 上级ID */
    private Long parentId;

    /** 祖级列表 */
    private String ancestors;

    /** 真实姓名 */
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 实名状态 */
    private String kycStatus;

    /** 等级ID */
    private Long levelId;

    /** 等级名称 */
    private String levelName;

    /** 状态 */
    private String status;

    /** 人民币可用 */
    private BigDecimal cnyAvailable;

    /** 人民币冻结 */
    private BigDecimal cnyFrozen;

    /** USDT可用 */
    private BigDecimal usdtAvailable;

    /** 团队人数 */
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

    public Integer getTeamCount()
    {
        return teamCount;
    }

    public void setTeamCount(Integer teamCount)
    {
        this.teamCount = teamCount;
    }

}
