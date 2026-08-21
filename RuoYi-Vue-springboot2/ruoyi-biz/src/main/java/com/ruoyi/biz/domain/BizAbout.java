package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * App关于我们（展示用，后台手改）
 */
public class BizAbout extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 内容ID */
    private Long aboutId;

    /** 标题 */
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 正文 */
    private String content;

    /** 可选配图 */
    private String imageUrl;

    /** 排序 */
    private Integer sort;

    /** 状态（0显示 1隐藏） */
    private String status;

    public Long getAboutId()
    {
        return aboutId;
    }

    public void setAboutId(Long aboutId)
    {
        this.aboutId = aboutId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
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
