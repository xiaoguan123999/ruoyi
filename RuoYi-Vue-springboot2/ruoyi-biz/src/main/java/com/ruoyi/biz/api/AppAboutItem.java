package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("关于我们")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppAboutItem
{
    @ApiModelProperty("展示模式 TEXT / PDF")
    private String mode;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("副标题")
    private String subtitle;
    @ApiModelProperty("正文纯文本，文本模式用")
    private String content;
    @ApiModelProperty("配图URL，文本模式用")
    private String imageUrl;
    @ApiModelProperty("PDF地址，PDF模式用")
    private String pdfUrl;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}
