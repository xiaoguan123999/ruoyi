package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 提现申请
 */
@ApiModel("提现单")
public class BizWithdraw extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 提现ID */
    @Excel(name = "单号")
    @ApiModelProperty("提现ID")
    private Long withdrawId;

    /** 会员ID */
    @Excel(name = "会员ID")
    @ApiModelProperty("会员ID")
    private Long memberId;

    /** 手机号 */
    @Excel(name = "手机号")
    @ApiModelProperty("手机号")
    private String phone;

    /** 姓名 */
    @Excel(name = "姓名")
    @ApiModelProperty("姓名")
    private String realName;

    /** 币种 */
    @Excel(name = "币种")
    @ApiModelProperty("币种")
    private String currency;

    /** 提现钱包类型 */
    @ApiModelProperty("提现钱包类型编码")
    private String walletTypeCode;

    @Excel(name = "钱包")
    @ApiModelProperty("提现钱包类型名称")
    private String walletTypeName;

    /** 金额 */
    @Excel(name = "金额")
    @ApiModelProperty("金额")
    private BigDecimal amount;

    @Excel(name = "手续费")
    @ApiModelProperty("fee amount")
    private BigDecimal feeAmount;

    @Excel(name = "到账")
    @ApiModelProperty("arrival amount")
    private BigDecimal arrivalAmount;

    /** 收款信息 */
    @Excel(name = "收款信息")
    @ApiModelProperty("收款信息")
    private String accountInfo;

    /** 收款方式 ALIPAY/BANK/USDT */
    @ApiModelProperty("收款方式 ALIPAY/BANK/USDT")
    private String payMethod;

    @Excel(name = "收款方式")
    private String payMethodLabel;

    /** 状态 0审核中 3待打款 1提现成功 2提现失败 */
    @Excel(name = "状态", readConverterExp = "0=审核中,3=待打款,1=提现成功,2=提现失败")
    @ApiModelProperty("状态：0审核中 3待打款 1提现成功 2提现失败")
    private String status;

    /** 审核人 */
    @Excel(name = "审核人")
    @ApiModelProperty("审核人")
    private String auditBy;

    /** 审核时间 */
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核备注 */
    @Excel(name = "审核备注")
    @ApiModelProperty("审核备注")
    private String auditRemark;

    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 打款凭证 */
    @ApiModelProperty("打款凭证")
    private String payProofUrl;

    @ApiModelProperty("勾选导出的提现单号")
    private Long[] withdrawIds;

    public Long getWithdrawId()
    {
        return withdrawId;
    }

    public void setWithdrawId(Long withdrawId)
    {
        this.withdrawId = withdrawId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getWalletTypeCode()
    {
        return walletTypeCode;
    }

    public void setWalletTypeCode(String walletTypeCode)
    {
        this.walletTypeCode = walletTypeCode;
    }

    public String getWalletTypeName()
    {
        return walletTypeName;
    }

    public void setWalletTypeName(String walletTypeName)
    {
        this.walletTypeName = walletTypeName;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getFeeAmount()
    {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount)
    {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getArrivalAmount()
    {
        return arrivalAmount;
    }

    public void setArrivalAmount(BigDecimal arrivalAmount)
    {
        this.arrivalAmount = arrivalAmount;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    public String getPayProofUrl()
    {
        return payProofUrl;
    }

    public void setPayProofUrl(String payProofUrl)
    {
        this.payProofUrl = payProofUrl;
    }

    @ApiModelProperty("状态文案：审核中 / 待打款 / 提现成功 / 提现失败")
    public String getStatusLabel()
    {
        return statusLabelOf(status);
    }

    public static String statusLabelOf(String status)
    {
        if ("0".equals(status))
        {
            return "审核中";
        }
        if ("3".equals(status))
        {
            return "待打款";
        }
        if ("1".equals(status))
        {
            return "提现成功";
        }
        if ("2".equals(status))
        {
            return "提现失败";
        }
        return status;
    }

    @ApiModelProperty("收款方式文案：支付宝 / 银行卡 / USDT")
    public String getPayMethodLabel()
    {
        if (payMethodLabel != null && payMethodLabel.length() > 0)
        {
            return payMethodLabel;
        }
        return payMethodLabelOf(payMethod, accountInfo, getRemark());
    }

    public void setPayMethodLabel(String payMethodLabel)
    {
        this.payMethodLabel = payMethodLabel;
    }

    public void fillPayMethodLabel()
    {
        this.payMethodLabel = payMethodLabelOf(payMethod, accountInfo, getRemark());
    }

    public Date getApplyTime()
    {
        return applyTime != null ? applyTime : getCreateTime();
    }

    public void setApplyTime(Date applyTime)
    {
        this.applyTime = applyTime;
    }

    public static String payMethodLabelOf(String payMethod, String accountInfo)
    {
        return payMethodLabelOf(payMethod, accountInfo, null);
    }

    public static String payMethodLabelOf(String payMethod, String accountInfo, String remark)
    {
        String method = payMethod == null ? "" : payMethod.trim().toUpperCase();
        if ("USDT".equals(method))
        {
            return "USDT";
        }
        if ("BANK".equals(method) || "PAY_BANK".equals(method))
        {
            return "银行卡";
        }
        if (looksLikeBank(accountInfo) || looksLikeBank(remark))
        {
            return "银行卡";
        }
        if ("ALIPAY".equals(method) || method.length() == 0)
        {
            return "支付宝";
        }
        return payMethod;
    }

    public static boolean looksLikeBank(String text)
    {
        if (text == null || text.length() == 0)
        {
            return false;
        }
        if (text.contains("银行") || text.contains("储蓄") || text.contains("信用社")
                || text.contains("银行卡") || text.contains("卡号") || text.contains("尾号")
                || text.contains("开户") || text.contains("借记") || text.contains("支行")
                || text.contains("工行") || text.contains("农行") || text.contains("建行")
                || text.contains("中行") || text.contains("交行") || text.contains("招行")
                || text.contains("邮储") || text.contains("浦发") || text.contains("民生")
                || text.contains("兴业") || text.contains("中信") || text.contains("光大")
                || text.contains("华夏") || text.contains("广发"))
        {
            return true;
        }
        return text.matches("(?s).*\\d{16,19}.*");
    }

    public Long[] getWithdrawIds()
    {
        return withdrawIds;
    }

    public void setWithdrawIds(Long[] withdrawIds)
    {
        this.withdrawIds = withdrawIds;
    }

}
