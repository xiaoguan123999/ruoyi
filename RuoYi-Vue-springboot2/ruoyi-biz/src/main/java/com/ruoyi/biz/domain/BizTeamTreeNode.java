package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("会员结构图节点")
public class BizTeamTreeNode
{
    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("上级ID")
    private Long parentId;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("姓名")
    private String realName;

    @ApiModelProperty("展示：ID / 手机号 / 姓名")
    private String label;

    @ApiModelProperty("直推人数")
    private Integer childCount;

    @ApiModelProperty("是否有下级")
    private Boolean hasChildren;

    @ApiModelProperty("el-tree 叶子")
    private Boolean leaf;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
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

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Integer getChildCount()
    {
        return childCount;
    }

    public void setChildCount(Integer childCount)
    {
        this.childCount = childCount;
    }

    public Boolean getHasChildren()
    {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren)
    {
        this.hasChildren = hasChildren;
    }

    public Boolean getLeaf()
    {
        return leaf;
    }

    public void setLeaf(Boolean leaf)
    {
        this.leaf = leaf;
    }
}
