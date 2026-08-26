package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizPayChannel;

public interface BizPayChannelMapper
{
    BizPayChannel selectPayChannelById(Long channelId);
    BizPayChannel selectPayChannelByCode(String channelCode);
    List<BizPayChannel> selectPayChannelList(BizPayChannel query);
    List<BizPayChannel> selectOperationalChannels(BizPayChannel query);
    int updatePayChannel(BizPayChannel row);
}
