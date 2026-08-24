package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("等级奖励发放操作")
public class BizLevelRewardPayBody
{
    @ApiModelProperty("发放记录ID，确认/拒绝待发放时用")
    private Long grantId;
    @ApiModelProperty("会员ID，永久档额外发放时用")
    private Long memberId;
    @ApiModelProperty("等级ID，永久档额外发放时用")
    private Long levelId;
    @ApiModelProperty("备注")
    private String remark;

    public Long getGrantId() { return grantId; }
    public void setGrantId(Long grantId) { this.grantId = grantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
