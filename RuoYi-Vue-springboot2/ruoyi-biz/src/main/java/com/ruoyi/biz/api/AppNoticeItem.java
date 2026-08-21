package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("公告")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppNoticeItem
{
    @ApiModelProperty(value = "公告ID")
    private Long noticeId;
    @ApiModelProperty(value = "标题")
    private String noticeTitle;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "发布时间")
    private Date createTime;
    @ApiModelProperty(value = "正文纯文本，仅详情接口返回")
    private String noticeContent;

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getNoticeContent() { return noticeContent; }
    public void setNoticeContent(String noticeContent) { this.noticeContent = noticeContent; }
}
