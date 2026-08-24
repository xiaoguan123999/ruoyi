package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("App首页视频轮播")
public class BizCarousel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("轮播ID")
    private Long carouselId;

    @ApiModelProperty("后台备注标题")
    private String title;

    @ApiModelProperty("视频地址")
    private String videoUrl;

    @ApiModelProperty("封面图")
    private String coverUrl;

    @ApiModelProperty("排序，越小越靠前")
    private Integer sort;

    @ApiModelProperty("0显示 1隐藏")
    private String status;

    public Long getCarouselId()
    {
        return carouselId;
    }

    public void setCarouselId(Long carouselId)
    {
        this.carouselId = carouselId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
