package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("客服渠道")
public class BizCsChannel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("渠道ID")
    private Long channelId;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("PHONE/WECHAT/TELEGRAM/QQ/LINK/QR")
    private String channelType;
    @ApiModelProperty("手机号/微信号/账号")
    private String value;
    @ApiModelProperty("二维码")
    private String qrUrl;
    @ApiModelProperty("跳转链接")
    private String linkUrl;
    @ApiModelProperty("排序")
    private Integer sort;
    @ApiModelProperty("0显示 1隐藏")
    private String status;

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
