package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("黑名单拦截记录")
public class BizBlacklistLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("记录ID")
    private Long logId;
    @ApiModelProperty("命中的黑名单ID")
    private Long blacklistId;
    @ApiModelProperty("LOGIN/REGISTER/KYC/BANK")
    private String action;
    @ApiModelProperty("PHONE/ID_CARD/BANK_CARD")
    private String hitType;
    @ApiModelProperty("命中值")
    private String hitValue;
    @ApiModelProperty("会员ID")
    private Long memberId;
    @ApiModelProperty("当时手机号")
    private String phone;
    @ApiModelProperty("当时姓名")
    private String realName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getBlacklistId() { return blacklistId; }
    public void setBlacklistId(Long blacklistId) { this.blacklistId = blacklistId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getHitType() { return hitType; }
    public void setHitType(String hitType) { this.hitType = hitType; }
    public String getHitValue() { return hitValue; }
    public void setHitValue(String hitValue) { this.hitValue = hitValue; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
