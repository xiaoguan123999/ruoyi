package com.ruoyi.biz.domain;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("客服中心配置")
public class BizCsConfig
{
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("工作时间")
    private String workTime;
    @ApiModelProperty("提示文案")
    private String hint;
    @ApiModelProperty("渠道列表")
    private List<BizCsChannel> channels;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getWorkTime() { return workTime; }
    public void setWorkTime(String workTime) { this.workTime = workTime; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public List<BizCsChannel> getChannels() { return channels; }
    public void setChannels(List<BizCsChannel> channels) { this.channels = channels; }
}
