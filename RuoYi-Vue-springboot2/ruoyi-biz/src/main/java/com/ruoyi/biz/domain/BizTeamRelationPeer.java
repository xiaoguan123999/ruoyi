package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("推荐关系图同级节点")
public class BizTeamRelationPeer
{
    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("账号/手机号")
    private String phone;

    @ApiModelProperty("是否当前路径上的人，前端标红")
    private Boolean current;

    public static BizTeamRelationPeer of(Long memberId, String phone, boolean current)
    {
        BizTeamRelationPeer p = new BizTeamRelationPeer();
        p.memberId = memberId;
        p.phone = phone;
        p.current = Boolean.valueOf(current);
        return p;
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

    public Boolean getCurrent()
    {
        return current;
    }

    public void setCurrent(Boolean current)
    {
        this.current = current;
    }
}
