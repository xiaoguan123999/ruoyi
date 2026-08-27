package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("后台钱包调账")
public class BizWalletAdjustBody
{
    @ApiModelProperty(value = "会员ID", required = true, example = "10003")
    private Long memberId;

    @ApiModelProperty(value = "钱包类型 BALANCE/PRODUCT/PROMO/ASSIST", required = true, example = "BALANCE")
    private String typeCode;

    @ApiModelProperty(value = "币种 CNY/USDT", required = true, example = "CNY")
    private String currency;

    @ApiModelProperty(value = "方向 PLUS增加 MINUS减少", required = true, example = "PLUS")
    private String direction;

    @ApiModelProperty(value = "金额，必须大于0", required = true, example = "100.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "备注", required = true, example = "客服补发")
    private String remark;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getDirection()
    {
        return direction;
    }

    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
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
