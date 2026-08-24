package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.api.AppServiceData;
import com.ruoyi.biz.domain.BizCsChannel;
import com.ruoyi.biz.domain.BizCsConfig;

public interface IBizCsService
{
    BizCsConfig getConfig();

    void saveConfig(BizCsConfig config);

    AppServiceData getAppService();

    BizCsChannel selectChannelById(Long channelId);

    List<BizCsChannel> selectChannelList(BizCsChannel query);

    int insertChannel(BizCsChannel channel);

    int updateChannel(BizCsChannel channel);

    int deleteChannelByIds(Long[] channelIds);
}
