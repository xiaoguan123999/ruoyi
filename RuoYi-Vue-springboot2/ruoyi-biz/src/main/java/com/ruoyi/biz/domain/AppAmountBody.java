package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("App金额类请求")
public class AppAmountBody
{
    @ApiModelProperty(value = "产品ID，认购时必填", example = "1")
    private Long productId;

    @ApiModelProperty(value = "币种 CNY/USDT。认购时按此扣对应钱包；不传则有人民币价走人民币，否则走USDT", example = "CNY")
    private String currency;

    @ApiModelProperty(value = "金额", example = "105")
    private BigDecimal amount;

    @ApiModelProperty(value = "提现账户信息")
    private String accountInfo;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "谷歌验证码，提现时按规则必填", example = "123456")
    private String googleCode;

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

    public String getGoogleCode()
    {
        return googleCode;
    }

    public void setGoogleCode(String googleCode)
    {
        this.googleCode = googleCode;
    }
}
