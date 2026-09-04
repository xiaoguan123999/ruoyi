package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("会员结构图节点")
public class BizTeamTreeNode
{
    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("上级ID")
    private Long parentId;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("姓名")
    private String realName;

    @ApiModelProperty("身份证")
    private String idCard;

    @ApiModelProperty("实名状态")
    private String kycStatus;

    @ApiModelProperty("展示：ID / 手机号 / 姓名")
    private String label;

    @ApiModelProperty("直推人数")
    private Integer childCount;

    @ApiModelProperty("下级总人数")
    private Integer teamCount;

    @ApiModelProperty("本人充值 CNY")
    private BigDecimal rechargeCny;

    @ApiModelProperty("本人充值 USDT")
    private BigDecimal rechargeUsdt;

    @ApiModelProperty("本人认购 CNY")
    private BigDecimal subscribeCny;

    @ApiModelProperty("本人认购 USDT")
    private BigDecimal subscribeUsdt;

    @ApiModelProperty("本人提现 CNY")
    private BigDecimal withdrawCny;

    @ApiModelProperty("本人提现 USDT")
    private BigDecimal withdrawUsdt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("最后登录时间")
    private Date lastLoginTime;

    @ApiModelProperty("最后登录IP")
    private String lastLoginIp;

    @ApiModelProperty("注册IP")
    private String registerIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("注册时间")
    private Date createTime;

    @ApiModelProperty("是否有下级")
    private Boolean hasChildren;

    @ApiModelProperty("el-tree 叶子")
    private Boolean leaf;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
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

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Integer getChildCount()
    {
        return childCount;
    }

    public void setChildCount(Integer childCount)
    {
        this.childCount = childCount;
    }

    public Integer getTeamCount()
    {
        return teamCount;
    }

    public void setTeamCount(Integer teamCount)
    {
        this.teamCount = teamCount;
    }

    public BigDecimal getRechargeCny()
    {
        return rechargeCny;
    }

    public void setRechargeCny(BigDecimal rechargeCny)
    {
        this.rechargeCny = rechargeCny;
    }

    public BigDecimal getRechargeUsdt()
    {
        return rechargeUsdt;
    }

    public void setRechargeUsdt(BigDecimal rechargeUsdt)
    {
        this.rechargeUsdt = rechargeUsdt;
    }

    public BigDecimal getSubscribeCny()
    {
        return subscribeCny;
    }

    public void setSubscribeCny(BigDecimal subscribeCny)
    {
        this.subscribeCny = subscribeCny;
    }

    public BigDecimal getSubscribeUsdt()
    {
        return subscribeUsdt;
    }

    public void setSubscribeUsdt(BigDecimal subscribeUsdt)
    {
        this.subscribeUsdt = subscribeUsdt;
    }

    public BigDecimal getWithdrawCny()
    {
        return withdrawCny;
    }

    public void setWithdrawCny(BigDecimal withdrawCny)
    {
        this.withdrawCny = withdrawCny;
    }

    public BigDecimal getWithdrawUsdt()
    {
        return withdrawUsdt;
    }

    public void setWithdrawUsdt(BigDecimal withdrawUsdt)
    {
        this.withdrawUsdt = withdrawUsdt;
    }

    public Date getLastLoginTime()
    {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime)
    {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp()
    {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp)
    {
        this.lastLoginIp = lastLoginIp;
    }

    public String getRegisterIp()
    {
        return registerIp;
    }

    public void setRegisterIp(String registerIp)
    {
        this.registerIp = registerIp;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Boolean getHasChildren()
    {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren)
    {
        this.hasChildren = hasChildren;
    }

    public Boolean getLeaf()
    {
        return leaf;
    }

    public void setLeaf(Boolean leaf)
    {
        this.leaf = leaf;
    }
}
