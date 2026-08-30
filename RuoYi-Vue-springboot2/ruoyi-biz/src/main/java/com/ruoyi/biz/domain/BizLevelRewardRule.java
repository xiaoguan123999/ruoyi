package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("成长激励金全局规则")
public class BizLevelRewardRule
{
    @ApiModelProperty("总开关")
    private Boolean enabled;
    @ApiModelProperty("发放币种：CNY/USDT 二选一，BOTH 两种金额都发")
    private String mixedPayCurrency;
    @ApiModelProperty("团队业绩口径：SUBSCRIBE认购 RECHARGE充值 BOTH两者相加")
    private String performanceSource;
    @ApiModelProperty("团队业绩是否含本人")
    private Boolean includeSelf;
    @ApiModelProperty("有效成员是否必须实名")
    private Boolean validNeedKyc;
    @ApiModelProperty("有效成员是否必须有认购")
    private Boolean validNeedOrder;
    @ApiModelProperty("规则说明文案")
    private String ruleText;
    @ApiModelProperty("App等级页表格上方注释")
    private String hint;
    @ApiModelProperty("1 USDT 折合多少人民币，默认 6.25")
    private java.math.BigDecimal usdtToCny;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getMixedPayCurrency() { return mixedPayCurrency; }
    public void setMixedPayCurrency(String mixedPayCurrency) { this.mixedPayCurrency = mixedPayCurrency; }
    public String getPerformanceSource() { return performanceSource; }
    public void setPerformanceSource(String performanceSource) { this.performanceSource = performanceSource; }
    public Boolean getIncludeSelf() { return includeSelf; }
    public void setIncludeSelf(Boolean includeSelf) { this.includeSelf = includeSelf; }
    public Boolean getValidNeedKyc() { return validNeedKyc; }
    public void setValidNeedKyc(Boolean validNeedKyc) { this.validNeedKyc = validNeedKyc; }
    public Boolean getValidNeedOrder() { return validNeedOrder; }
    public void setValidNeedOrder(Boolean validNeedOrder) { this.validNeedOrder = validNeedOrder; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public java.math.BigDecimal getUsdtToCny() { return usdtToCny; }
    public void setUsdtToCny(java.math.BigDecimal usdtToCny) { this.usdtToCny = usdtToCny; }
}
