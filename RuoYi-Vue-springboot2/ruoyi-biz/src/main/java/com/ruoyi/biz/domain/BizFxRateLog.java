package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("USDT折合人民币变更记录")
public class BizFxRateLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("记录ID")
    private Long logId;
    @ApiModelProperty("变更前汇率")
    private BigDecimal oldRate;
    @ApiModelProperty("变更后汇率")
    private BigDecimal newRate;
    @ApiModelProperty("操作人")
    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public BigDecimal getOldRate() { return oldRate; }
    public void setOldRate(BigDecimal oldRate) { this.oldRate = oldRate; }
    public BigDecimal getNewRate() { return newRate; }
    public void setNewRate(BigDecimal newRate) { this.newRate = newRate; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
