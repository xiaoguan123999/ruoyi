package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 会员钱包
 */
public class BizWallet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 钱包ID */
    private Long walletId;

    /** 会员ID */
    private Long memberId;

    /** 钱包类型编码 */
    private String typeCode;

    /** 钱包类型名称 */
    private String typeName;

    /** 币种 */
    private String currency;

    /** 可用 */
    private BigDecimal available;

    /** 冻结 */
    private BigDecimal frozen;

    public Long getWalletId()
    {
        return walletId;
    }

    public void setWalletId(Long walletId)
    {
        this.walletId = walletId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public BigDecimal getAvailable()
    {
        return available;
    }

    public void setAvailable(BigDecimal available)
    {
        this.available = available;
    }

    public BigDecimal getFrozen()
    {
        return frozen;
    }

    public void setFrozen(BigDecimal frozen)
    {
        this.frozen = frozen;
    }

}
