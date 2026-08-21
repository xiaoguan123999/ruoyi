package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * App官方群聊（展示用，后台上传二维码）
 */
public class BizGroupChat extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long groupId;

    private String title;

    private String hint;

    private String qrUrl;

    private Integer sort;

    private String status;

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getHint()
    {
        return hint;
    }

    public void setHint(String hint)
    {
        this.hint = hint;
    }

    public String getQrUrl()
    {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl)
    {
        this.qrUrl = qrUrl;
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
