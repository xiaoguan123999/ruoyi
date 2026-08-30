package com.ruoyi.biz.api;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("可领取的等级奖励")
public class AppLevelRewardClaimItem
{
    @ApiModelProperty("等级ID")
    private Long levelId;
    @ApiModelProperty("等级名称")
    private String levelName;
    @ApiModelProperty("ONE 二选一 ALL 都可领取")
    private String claimPolicy;
    @ApiModelProperty("到账钱包")
    private String walletTypeCode;
    @ApiModelProperty("当前周期还可领的币种和金额")
    private List<AppLevelRewardOption> options;
    @ApiModelProperty("本周期已领过的币种")
    private List<String> claimedCurrencies;

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public String getClaimPolicy() { return claimPolicy; }
    public void setClaimPolicy(String claimPolicy) { this.claimPolicy = claimPolicy; }
    public String getWalletTypeCode() { return walletTypeCode; }
    public void setWalletTypeCode(String walletTypeCode) { this.walletTypeCode = walletTypeCode; }
    public List<AppLevelRewardOption> getOptions() { return options; }
    public void setOptions(List<AppLevelRewardOption> options) { this.options = options; }
    public List<String> getClaimedCurrencies() { return claimedCurrencies; }
    public void setClaimedCurrencies(List<String> claimedCurrencies) { this.claimedCurrencies = claimedCurrencies; }
}
