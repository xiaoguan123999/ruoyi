package com.ruoyi.biz.api;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("资金流水")
public class AppWalletLogItem
{
    @ApiModelProperty("流水ID")
    private Long logId;
    @ApiModelProperty("同 logId，方便前端当 id 用")
    private Long id;
    @ApiModelProperty("展示标题，如产品名、充值、推广奖金")
    private String title;
    @ApiModelProperty("title 别名")
    private String name;
    @ApiModelProperty("业务类型")
    private String bizType;
    @ApiModelProperty("钱包类型")
    private String typeCode;
    @ApiModelProperty("类型中文")
    private String bizTypeLabel;
    @ApiModelProperty("bizTypeLabel 别名")
    private String typeLabel;
    @ApiModelProperty("变动金额，收入为正、支出为负")
    private BigDecimal amount;
    @ApiModelProperty("币种 CNY/USDT")
    private String currency;
    @ApiModelProperty("IN 收入 OUT 支出")
    private String direction;
    @ApiModelProperty("yyyy-MM-dd")
    private String date;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @ApiModelProperty("备注")
    private String remark;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getBizTypeLabel() { return bizTypeLabel; }
    public void setBizTypeLabel(String bizTypeLabel) { this.bizTypeLabel = bizTypeLabel; }
    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
