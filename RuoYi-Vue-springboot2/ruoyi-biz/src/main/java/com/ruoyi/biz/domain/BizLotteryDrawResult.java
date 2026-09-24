package com.ruoyi.biz.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 大转盘抽奖结果
 */
@ApiModel("大转盘抽奖结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BizLotteryDrawResult
{
    @ApiModelProperty("奖品ID，熔断时可空")
    private Long prizeId;

    @ApiModelProperty("奖品名称")
    private String prizeName;

    @ApiModelProperty("奖品类型")
    private Integer prizeType;

    @ApiModelProperty("扇区位置")
    private Integer position;

    @ApiModelProperty("是否库存熔断")
    private Boolean isMeltdown;

    @ApiModelProperty("是否步进必中")
    private Boolean isStrategy;

    @ApiModelProperty("提示文案")
    private String msg;

    public Long getPrizeId() { return prizeId; }
    public void setPrizeId(Long prizeId) { this.prizeId = prizeId; }
    public String getPrizeName() { return prizeName; }
    public void setPrizeName(String prizeName) { this.prizeName = prizeName; }
    public Integer getPrizeType() { return prizeType; }
    public void setPrizeType(Integer prizeType) { this.prizeType = prizeType; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getIsMeltdown() { return isMeltdown; }
    public void setIsMeltdown(Boolean isMeltdown) { this.isMeltdown = isMeltdown; }
    public Boolean getIsStrategy() { return isStrategy; }
    public void setIsStrategy(Boolean isStrategy) { this.isStrategy = isStrategy; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
}
