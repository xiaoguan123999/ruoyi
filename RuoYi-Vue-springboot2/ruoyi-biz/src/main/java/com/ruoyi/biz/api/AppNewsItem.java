package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("新闻")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppNewsItem
{
    @ApiModelProperty(value = "新闻ID", example = "1")
    private Long newsId;
    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "摘要，列表用")
    private String summary;
    @ApiModelProperty(value = "封面图URL，空则 App 用本地默认图")
    private String coverUrl;
    @ApiModelProperty(value = "发布日期 yyyy-MM-dd", example = "2026-08-18")
    private String publishDate;
    @ApiModelProperty(value = "排序，越小越靠前")
    private Integer sort;
    @ApiModelProperty(value = "正文纯文本，仅详情接口返回")
    private String content;

    public Long getNewsId() { return newsId; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
