package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("邀请信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppInviteData
{
    @ApiModelProperty(value = "我的7位邀请码", example = "5839201")
    private String inviteCode;
    @ApiModelProperty(value = "直推人数")
    private Integer inviteCount;
    @ApiModelProperty(value = "邀请奖励，当前固定 0")
    private Integer reward;

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public Integer getInviteCount() { return inviteCount; }
    public void setInviteCount(Integer inviteCount) { this.inviteCount = inviteCount; }
    public Integer getReward() { return reward; }
    public void setReward(Integer reward) { this.reward = reward; }
}
