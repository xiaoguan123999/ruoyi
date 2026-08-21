package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizGroupChat;

public interface BizGroupChatMapper
{
    BizGroupChat selectGroupChatById(Long groupId);

    List<BizGroupChat> selectGroupChatList(BizGroupChat groupChat);

    int insertGroupChat(BizGroupChat groupChat);

    int updateGroupChat(BizGroupChat groupChat);

    int deleteGroupChatByIds(Long[] groupIds);
}
