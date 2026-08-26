package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("会员登录日志")
public class BizMemberLogininfor extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "访问编号", cellType = ColumnType.NUMERIC)
    @ApiModelProperty("访问ID")
    private Long infoId;

    @Excel(name = "会员ID", cellType = ColumnType.NUMERIC)
    @ApiModelProperty("会员ID")
    private Long memberId;

    @Excel(name = "手机号")
    @ApiModelProperty("手机号")
    private String phone;

    @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
    @ApiModelProperty("登录状态 0成功 1失败")
    private String status;

    @Excel(name = "登录地址")
    @ApiModelProperty("登录IP")
    private String ipaddr;

    @Excel(name = "登录地点")
    @ApiModelProperty("登录地点")
    private String loginLocation;

    @Excel(name = "浏览器")
    @ApiModelProperty("浏览器")
    private String browser;

    @Excel(name = "操作系统")
    @ApiModelProperty("操作系统")
    private String os;

    @Excel(name = "提示消息")
    @ApiModelProperty("提示消息")
    private String msg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("访问时间")
    private Date loginTime;

    public Long getInfoId() { return infoId; }
    public void setInfoId(Long infoId) { this.infoId = infoId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIpaddr() { return ipaddr; }
    public void setIpaddr(String ipaddr) { this.ipaddr = ipaddr; }
    public String getLoginLocation() { return loginLocation; }
    public void setLoginLocation(String loginLocation) { this.loginLocation = loginLocation; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public Date getLoginTime() { return loginTime; }
    public void setLoginTime(Date loginTime) { this.loginTime = loginTime; }
}
