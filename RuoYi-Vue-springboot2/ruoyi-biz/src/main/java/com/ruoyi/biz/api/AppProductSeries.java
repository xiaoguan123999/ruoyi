package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("产品系列（后台产品分类）")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppProductSeries
{
    @ApiModelProperty(value = "系列ID，点进去查产品时带这个", example = "1")
    private Long seriesId;

    @ApiModelProperty(value = "系列名称，Tab 卡片标题", example = "「星帆·天启计划」")
    private String seriesName;

    @ApiModelProperty(value = "封面图URL，空则 App 用本地默认图")
    private String coverUrl;

    @ApiModelProperty(value = "排序，越小越靠前")
    private Integer sort;

    public Long getSeriesId()
    {
        return seriesId;
    }

    public void setSeriesId(Long seriesId)
    {
        this.seriesId = seriesId;
    }

    public Long getCategoryId()
    {
        return seriesId;
    }

    public String getSeriesName()
    {
        return seriesName;
    }

    public void setSeriesName(String seriesName)
    {
        this.seriesName = seriesName;
    }

    public String getCategoryName()
    {
        return seriesName;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
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
