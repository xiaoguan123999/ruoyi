package com.ruoyi.biz.api;

import java.util.List;

public class AppPayChannelListResult extends AppDataResult<List<AppPayChannelItem>>
{
    public static AppPayChannelListResult ok(List<AppPayChannelItem> data)
    {
        return fillOk(new AppPayChannelListResult(), data);
    }
}
