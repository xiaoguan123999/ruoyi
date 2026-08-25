package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @ApiModelProperty("提现ID")
    private Long withdrawId;

    /** 会员ID */
    @ApiModelProperty("会员ID")
    private Long memberId;

    /** 手机号 */
    @ApiModelProperty("手机号")
    private String phone;

    /** 姓名 */
    @ApiModelProperty("姓名")
    private String realName;

    /** 币种 */
    @ApiModelProperty("币种")
    private String currency;

    /** 金额 */
    @ApiModelProperty("金额")
    private BigDecimal amount;

    /** 收款信息 */
    @ApiModelProperty("收款信息")
    private String accountInfo;

    /** 收款方式 ALIPAY/USDT */
    @ApiModelProperty("收款方式 ALIPAY/USDT")
    private String payMethod;

    /** 状态 0待打款 1已打款 2已拒绝 */
    @ApiModelProperty("状态：0待打款 1已打款 2已拒绝")
    private String status;

    /** 审核人 */
    @ApiModelProperty("审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核备注 */
    @ApiModelProperty("审核备注")
    private String auditRemark;

    /** 打款凭证 */
    @ApiModelProperty("打款凭证")
    private String payProofUrl;

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

    @ApiModelProperty("状态文案：待打款 / 已打款 / 已拒绝")
    public String getStatusLabel()
    {
        if ("0".equals(status))
        {
            return "待打款";
        }
        if ("1".equals(status))
        {
            return "已打款";
        }
        if ("2".equals(status))
        {
            return "已拒绝";
        }
        return status;
    }

    @ApiModelProperty("收款方式文案：支付宝 / USDT")
    public String getPayMethodLabel()
    {
        if ("ALIPAY".equals(payMethod))
        {
            return "支付宝";
        }
        if ("USDT".equals(payMethod))
        {
            return "USDT";
        }
        if ("BANK".equals(payMethod) || "PAY_BANK".equals(payMethod))
        {
            return "银行卡";
        }
        return payMethod;
    }

}
