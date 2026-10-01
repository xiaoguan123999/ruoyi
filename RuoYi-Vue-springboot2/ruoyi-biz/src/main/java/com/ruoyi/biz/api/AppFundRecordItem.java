package com.ruoyi.biz.api;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("资金申请记录")
public class AppFundRecordItem
{
    @ApiModelProperty("记录ID")
    private Long id;
    @ApiModelProperty("类型 RECHARGE/WITHDRAW")
    private String bizType;
    @ApiModelProperty("类型中文")
    private String bizTypeLabel;
    @ApiModelProperty("bizTypeLabel 别名")
    private String typeLabel;
    @ApiModelProperty("展示标题，如充值已通过")
    private String title;
    @ApiModelProperty("title 别名")
    private String name;
    @ApiModelProperty("申请金额")
    private BigDecimal amount;
    @ApiModelProperty("币种")
    private String currency;
    @ApiModelProperty("状态 0待处理 1成功 2拒绝")
    private String status;
    @ApiModelProperty("状态中文")
    private String statusLabel;
    @ApiModelProperty("收款信息，提现才有")
    private String accountInfo;
    @ApiModelProperty("收款方式")
    private String payMethod;
    @ApiModelProperty("收款方式中文")
    private String payMethodLabel;
    @ApiModelProperty("打款凭证")
    private String payProofUrl;
    @ApiModelProperty("0人工 1线上")
    private String payMode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    @ApiModelProperty("审核备注")
    private String auditRemark;
    @ApiModelProperty("备注")
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizTypeLabel() { return bizTypeLabel; }
    public void setBizTypeLabel(String bizTypeLabel) { this.bizTypeLabel = bizTypeLabel; }
    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public String getAccountInfo() { return accountInfo; }
    public void setAccountInfo(String accountInfo) { this.accountInfo = accountInfo; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public String getPayMethodLabel() { return payMethodLabel; }
    public void setPayMethodLabel(String payMethodLabel) { this.payMethodLabel = payMethodLabel; }
    public String getPayProofUrl() { return payProofUrl; }
    public void setPayProofUrl(String payProofUrl) { this.payProofUrl = payProofUrl; }
    public String getPayMode() { return payMode; }
    public void setPayMode(String payMode) { this.payMode = payMode; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
