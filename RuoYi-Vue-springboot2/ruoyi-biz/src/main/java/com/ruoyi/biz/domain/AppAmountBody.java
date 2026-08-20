package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("App金额类请求")
public class AppAmountBody
{
    @ApiModelProperty(value = "产品ID，认购时必填", example = "1")
    private Long productId;

    @ApiModelProperty(value = "币种，默认CNY", example = "CNY")
    private String currency;

    @ApiModelProperty(value = "金额", example = "105")
    private BigDecimal amount;

    @ApiModelProperty(value = "提现账户信息")
    private String accountInfo;

    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
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

    public String getAccountInfo()
    {
        return accountInfo;
    }

    public void setAccountInfo(String accountInfo)
    {
        this.accountInfo = accountInfo;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
