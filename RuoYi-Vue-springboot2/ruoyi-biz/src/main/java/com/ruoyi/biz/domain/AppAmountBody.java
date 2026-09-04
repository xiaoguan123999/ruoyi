package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

@ApiModel("App金额类请求")
public class AppAmountBody
{
    @ApiModelProperty(value = "产品ID，认购时必填", example = "1")
    private Long productId;

    @ApiModelProperty(value = "认购份数，默认 1", example = "1")
    @JsonAlias({"qty", "count", "num"})
    private Integer quantity;

    @ApiModelProperty(value = "币种 CNY/USDT。认购时按此扣对应钱包；不传则有人民币价走人民币，否则走USDT", example = "CNY")
    private String currency;

    @ApiModelProperty(value = "金额", example = "105")
    private BigDecimal amount;

    @ApiModelProperty(value = "已保存的收款账户ID，提现时可传")
    private Long accountId;

    @ApiModelProperty(value = "提现账户信息")
    private String accountInfo;

    @ApiModelProperty(value = "收款方式 ALIPAY/BANK/USDT，传 accountId 时以后台账户类型为准")
    private String payMethod;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "谷歌验证码，App 提现可省略", example = "123456")
    private String googleCode;

    @ApiModelProperty(value = "支付/交易密码，认购必填", example = "123456")
    @JsonAlias({"tradePassword", "fundPassword", "payPwd"})
    private String payPassword;

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
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

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getAccountInfo()
    {
        return accountInfo;
    }

    public void setAccountInfo(String accountInfo)
    {
        this.accountInfo = accountInfo;
    }

    public String getPayMethod()
    {
        return payMethod;
    }

    public void setPayMethod(String payMethod)
    {
        this.payMethod = payMethod;
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

    public String getPayPassword()
    {
        return payPassword;
    }

    public void setPayPassword(String payPassword)
    {
        this.payPassword = payPassword;
    }
}
