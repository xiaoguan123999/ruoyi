package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("首页视频轮播")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCarouselItem
{
    @ApiModelProperty(value = "轮播ID", example = "1")
    private Long carouselId;

    @ApiModelProperty(value = "标题，可空")
    private String title;

    @ApiModelProperty(value = "视频完整 URL")
    private String videoUrl;

    @ApiModelProperty(value = "封面图 URL，空则 App 可用首帧或本地图")
    private String coverUrl;

    @ApiModelProperty(value = "排序，越小越靠前")
    private Integer sort;

    public Long getCarouselId() { return carouselId; }
    public void setCarouselId(Long carouselId) { this.carouselId = carouselId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
