package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("后台换绑上级")
public class BizRebindParentBody
{
    @ApiModelProperty(value = "新上级会员ID。与 inviteCode 传一个即可", example = "10003")
    private Long parentId;

    @ApiModelProperty(value = "新上级邀请码或会员ID。与 parentId 传一个即可", example = "5839201")
    private String inviteCode;

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getInviteCode()
    {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }
}
