package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("产品分类/系列")
public class BizProductCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("分类ID，App 里就是系列ID")
    private Long categoryId;

    @ApiModelProperty("系列名称，Tab 卡片标题")
    private String categoryName;

    @ApiModelProperty("封面图，Tab 卡片用")
    private String coverUrl;

    @ApiModelProperty("0显示 1隐藏")
    private String status;

    @ApiModelProperty("排序，越小越靠前")
    private Integer sort;

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }
}
