package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("推荐关系图一行：从顶点到查询会员的路径上的一个人")
public class BizTeamRelationRow
{
    @Excel(name = "用户ID", cellType = ColumnType.NUMERIC, sort = 1)
    @ApiModelProperty("用户ID")
    private Long memberId;

    @Excel(name = "层级", cellType = ColumnType.NUMERIC, sort = 2)
    @ApiModelProperty("从网体顶点往下的层级，1 为该链条最顶")
    private Integer teamLevel;

    @Excel(name = "余额", sort = 3)
    @ApiModelProperty("CNY 可用余额")
    private BigDecimal balance;

    @Excel(name = "签到天数", cellType = ColumnType.NUMERIC, sort = 4)
    @ApiModelProperty("累计签到天数")
    private Integer checkinDays;

    @Excel(name = "账号", sort = 5)
    @ApiModelProperty("账号/手机号")
    private String account;

    @Excel(name = "列表", sort = 6)
    @ApiModelProperty("同级直推列表文本，导出用")
    private String peerList;

    @ApiModelProperty("同级直推，当前路径成员 current=true")
    private List<BizTeamRelationPeer> peers;

    public String getPeerList()
    {
        return peerList;
    }

    public void setPeerList(String peerList)
    {
        this.peerList = peerList;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Integer getTeamLevel()
    {
        return teamLevel;
    }

    public void setTeamLevel(Integer teamLevel)
    {
        this.teamLevel = teamLevel;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }

    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

    public Integer getCheckinDays()
    {
        return checkinDays;
    }

    public void setCheckinDays(Integer checkinDays)
    {
        this.checkinDays = checkinDays;
    }

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    public List<BizTeamRelationPeer> getPeers()
    {
        return peers;
    }

    public void setPeers(List<BizTeamRelationPeer> peers)
    {
        this.peers = peers;
    }
}
