package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * App关于我们（全局一条，文本/PDF 二选一）
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

    /** 正文（富文本） */
    private String content;

    /** 配图 */
    private String imageUrl;

    /** 展示模式 TEXT / PDF */
    private String mode;

    /** PDF 文件地址 */
    private String pdfUrl;

    /** 排序 */
    private Integer sort;

    /** 状态（0显示 1隐藏） */
    private String status;

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
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
