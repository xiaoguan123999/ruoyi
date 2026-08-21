package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("官方群聊")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppGroupChatItem
{
    @ApiModelProperty("记录ID")
    private Long groupId;
    @ApiModelProperty("标题，例如官方群聊")
    private String title;
    @ApiModelProperty("提示文案，例如扫码进群")
    private String hint;
    @ApiModelProperty("群二维码图片完整URL")
    private String qrUrl;
    @ApiModelProperty("补充说明")
    private String remark;
    @ApiModelProperty("排序")
    private Integer sort;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
