package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("运行概览卡片")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppOverviewItem
{
    @ApiModelProperty(value = "卡片键：satellite / coverage / terminal，App 用来匹配本地 3D 图")
    private String itemKey;
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "展示数值，例如 12")
    private String displayValue;
    @ApiModelProperty(value = "状态文案，例如 运行中")
    private String statusText;
    @ApiModelProperty(value = "状态颜色，例如 #22c55e")
    private String statusColor;
    @ApiModelProperty(value = "可选配图URL")
    private String imageUrl;
    @ApiModelProperty(value = "排序")
    private Integer sort;

    public String getItemKey() { return itemKey; }
    public void setItemKey(String itemKey) { this.itemKey = itemKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDisplayValue() { return displayValue; }
    public void setDisplayValue(String displayValue) { this.displayValue = displayValue; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getStatusColor() { return statusColor; }
    public void setStatusColor(String statusColor) { this.statusColor = statusColor; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
