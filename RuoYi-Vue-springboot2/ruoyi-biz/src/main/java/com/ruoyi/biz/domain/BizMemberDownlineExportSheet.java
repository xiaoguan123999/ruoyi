package com.ruoyi.biz.domain;

import java.util.List;

/**
 * 下级导出：标题行含充值合计，rows 为每人一行
 */
public class BizMemberDownlineExportSheet
{
    private String title;

    private List<BizMemberDownlineExportRow> rows;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public List<BizMemberDownlineExportRow> getRows()
    {
        return rows;
    }

    public void setRows(List<BizMemberDownlineExportRow> rows)
    {
        this.rows = rows;
    }
}
