package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("审核请求")
public class BizAuditBody
{
    @ApiModelProperty(value = "单据ID", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "审核结果，1通过 2拒绝", required = true, example = "1")
    private String status;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "打款凭证图片URL")
    private String payProofUrl;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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
}
