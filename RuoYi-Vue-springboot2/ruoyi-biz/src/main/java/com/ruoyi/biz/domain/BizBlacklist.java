package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("黑名单")
public class BizBlacklist extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("黑名单ID")
    private Long blacklistId;
    @ApiModelProperty("姓名")
    private String realName;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("身份证号")
    private String idCard;
    @ApiModelProperty("银行卡号")
    private String bankCard;
    @ApiModelProperty("0启用 1停用")
    private String status;

    public Long getBlacklistId() { return blacklistId; }
    public void setBlacklistId(Long blacklistId) { this.blacklistId = blacklistId; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getBankCard() { return bankCard; }
    public void setBankCard(String bankCard) { this.bankCard = bankCard; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
