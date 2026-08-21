package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("关于我们")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppAboutItem
{
    @ApiModelProperty("记录ID")
    private Long aboutId;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("副标题")
    private String subtitle;
    @ApiModelProperty("正文纯文本")
    private String content;
    @ApiModelProperty("配图URL")
    private String imageUrl;
    @ApiModelProperty("排序")
    private Integer sort;

    public Long getAboutId() { return aboutId; }
    public void setAboutId(Long aboutId) { this.aboutId = aboutId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
