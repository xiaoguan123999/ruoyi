package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;

/**
 * 下级导出行：所选账号整条线（不含本人）
 */
public class BizMemberDownlineExportRow
{
    @Excel(name = "会员ID", cellType = ColumnType.NUMERIC, sort = 1)
    private Long memberId;

    @Excel(name = "手机号", sort = 2)
    private String phone;

    @Excel(name = "姓名", sort = 3, defaultValue = "--")
    private String realName;

    @Excel(name = "下级人数", cellType = ColumnType.NUMERIC, sort = 4)
    private Integer downlineCount;

    @Excel(name = "激活人数", cellType = ColumnType.NUMERIC, sort = 5)
    private Integer activatedCount;

    @Excel(name = "充值CNY", sort = 6, scale = 2)
    private BigDecimal rechargeCny;

    @Excel(name = "充值USDT", sort = 7, scale = 2)
    private BigDecimal rechargeUsdt;

    @Excel(name = "认购产品", sort = 8, width = 80, wrapText = true)
    private String products;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public Integer getDownlineCount()
    {
        return downlineCount;
    }

    public void setDownlineCount(Integer downlineCount)
    {
        this.downlineCount = downlineCount;
    }

    public Integer getActivatedCount()
    {
        return activatedCount;
    }

    public void setActivatedCount(Integer activatedCount)
    {
        this.activatedCount = activatedCount;
    }

    public BigDecimal getRechargeCny()
    {
        return rechargeCny;
    }

    public void setRechargeCny(BigDecimal rechargeCny)
    {
        this.rechargeCny = rechargeCny;
    }

    public BigDecimal getRechargeUsdt()
    {
        return rechargeUsdt;
    }

    public void setRechargeUsdt(BigDecimal rechargeUsdt)
    {
        this.rechargeUsdt = rechargeUsdt;
    }

    public String getProducts()
    {
        return products;
    }

    public void setProducts(String products)
    {
        this.products = products;
    }
}
