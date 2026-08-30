package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("等级奖励领取结果")
public class AppLevelRewardClaimData
{
    @ApiModelProperty("等级ID")
    private Long levelId;
    @ApiModelProperty("等级名称")
    private String levelName;
    @ApiModelProperty("入账币种")
    private String currency;
    @ApiModelProperty("入账金额")
    private BigDecimal amount;
    @ApiModelProperty("到账钱包")
    private String walletTypeCode;

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getWalletTypeCode() { return walletTypeCode; }
    public void setWalletTypeCode(String walletTypeCode) { this.walletTypeCode = walletTypeCode; }
}
