package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizCsChannel;

public interface BizCsChannelMapper
{
    BizCsChannel selectChannelById(Long channelId);

    List<BizCsChannel> selectChannelList(BizCsChannel query);

    int insertChannel(BizCsChannel channel);

    int updateChannel(BizCsChannel channel);

    int deleteChannelByIds(Long[] channelIds);
}
