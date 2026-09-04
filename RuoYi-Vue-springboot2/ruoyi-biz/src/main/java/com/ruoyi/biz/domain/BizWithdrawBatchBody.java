package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("提现批量审核")
public class BizWithdrawBatchBody
{
    @ApiModelProperty(value = "提现单号，不传则按筛选条件处理全部")
    private Long[] ids;

    @ApiModelProperty(value = "目标状态：3待打款 1提现成功 2提现失败", required = true)
    private String status;

    @ApiModelProperty("审核备注，提现失败必填")
    private String auditRemark;

    @ApiModelProperty("打款凭证图片URL")
    private String payProofUrl;

    @ApiModelProperty("未勾选时按筛选条件处理：单号")
    private Long withdrawId;

    @ApiModelProperty("未勾选时按筛选条件处理：会员ID")
    private Long memberId;

    @ApiModelProperty("未勾选时按筛选条件处理：手机号")
    private String phone;

    @ApiModelProperty("未勾选时按筛选条件处理：币种")
    private String currency;

    @ApiModelProperty("未勾选时按筛选条件处理：当前列表状态")
    private String filterStatus;

    @ApiModelProperty("未勾选时按筛选条件处理：开始日期 yyyy-MM-dd")
    private String beginTime;

    @ApiModelProperty("未勾选时按筛选条件处理：结束日期 yyyy-MM-dd")
    private String endTime;

    public Long[] getIds()
    {
        return ids;
    }

    public void setIds(Long[] ids)
    {
        this.ids = ids;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getFilterStatus()
    {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus)
    {
        this.filterStatus = filterStatus;
    }

    public String getBeginTime()
    {
        return beginTime;
    }

    public void setBeginTime(String beginTime)
    {
        this.beginTime = beginTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }
}
