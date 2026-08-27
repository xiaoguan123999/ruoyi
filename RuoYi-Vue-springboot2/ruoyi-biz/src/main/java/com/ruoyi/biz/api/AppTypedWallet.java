package com.ruoyi.biz.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("按钱包类型+币种的资产")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppTypedWallet
{
    @ApiModelProperty("钱包类型编码")
    private String typeCode;
    @ApiModelProperty("钱包类型名称")
    private String typeName;
    @ApiModelProperty("币种")
    private String currency;
    @ApiModelProperty("可用")
    private BigDecimal available;
    @ApiModelProperty("冻结")
    private BigDecimal frozen;
    @ApiModelProperty("提现规则")
    private String withdrawMode;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAvailable() { return available; }
    public void setAvailable(BigDecimal available) { this.available = available; }
    public BigDecimal getFrozen() { return frozen; }
    public void setFrozen(BigDecimal frozen) { this.frozen = frozen; }
    public String getWithdrawMode() { return withdrawMode; }
    public void setWithdrawMode(String withdrawMode) { this.withdrawMode = withdrawMode; }
}
