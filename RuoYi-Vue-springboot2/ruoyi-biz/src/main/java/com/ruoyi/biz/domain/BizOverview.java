package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * App运行概览卡片（展示用，后台手改）
 */
public class BizOverview extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 卡片ID */
    private Long itemId;

    /** 卡片标识，App用它匹配本地图 */
    private String itemKey;

    /** 标题 */
    private String title;

    /** 展示数值，含单位 */
    private String displayValue;

    /** 状态文案 */
    private String statusText;

    /** 状态点颜色 */
    private String statusColor;

    /** 可选配图 */
    private String imageUrl;

    /** 排序 */
    private Integer sort;

    /** 状态（0显示 1隐藏） */
    private String status;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public String getItemKey()
    {
        return itemKey;
    }

    public void setItemKey(String itemKey)
    {
        this.itemKey = itemKey;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDisplayValue()
    {
        return displayValue;
    }

    public void setDisplayValue(String displayValue)
    {
        this.displayValue = displayValue;
    }

    public String getStatusText()
    {
        return statusText;
    }

    public void setStatusText(String statusText)
    {
        this.statusText = statusText;
    }

    public String getStatusColor()
    {
        return statusColor;
    }

    public void setStatusColor(String statusColor)
    {
        this.statusColor = statusColor;
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
