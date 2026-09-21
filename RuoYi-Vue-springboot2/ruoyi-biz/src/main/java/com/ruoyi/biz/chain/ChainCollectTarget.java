package com.ruoyi.biz.chain;

public class ChainCollectTarget
{
    private String address;
    private String source;
    private Long collectMemberId;

    public static ChainCollectTarget of(String address, String source, Long collectMemberId)
    {
        ChainCollectTarget t = new ChainCollectTarget();
        t.address = address;
        t.source = source;
        t.collectMemberId = collectMemberId;
        return t;
    }

    public String getAddress() { return address; }
    public String getSource() { return source; }
    public Long getCollectMemberId() { return collectMemberId; }
}
