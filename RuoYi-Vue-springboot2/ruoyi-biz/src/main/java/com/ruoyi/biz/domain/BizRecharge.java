package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 充值申请
 */
@ApiModel("充值单")
public class BizRecharge extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 充值ID */
    @Excel(name = "单号")
    @ApiModelProperty("充值ID")
    private Long rechargeId;

    /** 会员ID */
    @Excel(name = "会员ID")
    @ApiModelProperty("会员ID")
    private Long memberId;

    /** 手机号 */
    @Excel(name = "手机号")
    @ApiModelProperty("手机号")
    private String phone;

    /** 币种 */
    @Excel(name = "币种")
    @ApiModelProperty("币种")
    private String currency;

    /** 金额 */
    @Excel(name = "金额")
    @ApiModelProperty("金额")
    private BigDecimal amount;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=待审,1=通过,2=拒绝")
    @ApiModelProperty("审核状态：0待审 1通过已入账 2拒绝")
    private String status;

    /** 审核人 */
    @Excel(name = "审核人")
    @ApiModelProperty("审核人")
    private String auditBy;

    /** 审核时间 */
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核备注 */
    @Excel(name = "审核备注")
    @ApiModelProperty("审核备注")
    private String auditRemark;

    @Excel(name = "申请备注")
    private String applyRemark;

    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    @ApiModelProperty("0人工 1线上")
    private String payMode;

    @ApiModelProperty("支付通道")
    private String channelCode;

    @ApiModelProperty("线上商户单号")
    private String outTradeNo;

    @ApiModelProperty("勾选导出的充值单号")
    private Long[] rechargeIds;

    public Long getRechargeId()
    {
        return rechargeId;
    }

    public void setRechargeId(Long rechargeId)
    {
        this.rechargeId = rechargeId;
    }

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

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    public String getPayMode() { return payMode; }
    public void setPayMode(String payMode) { this.payMode = payMode; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }

    public String getApplyRemark() { return applyRemark != null ? applyRemark : getRemark(); }
    public void setApplyRemark(String applyRemark) { this.applyRemark = applyRemark; }
    public Date getApplyTime() { return applyTime != null ? applyTime : getCreateTime(); }
    public void setApplyTime(Date applyTime) { this.applyTime = applyTime; }

    public Long[] getRechargeIds()
    {
        return rechargeIds;
    }

    public void setRechargeIds(Long[] rechargeIds)
    {
        this.rechargeIds = rechargeIds;
    }

}
