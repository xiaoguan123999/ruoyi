package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizGroupChat;

public interface IBizGroupChatService
{
    BizGroupChat selectGroupChatById(Long groupId);

    List<BizGroupChat> selectGroupChatList(BizGroupChat groupChat);

    List<BizGroupChat> selectAppGroupChatList();

    int insertGroupChat(BizGroupChat groupChat);

    int updateGroupChat(BizGroupChat groupChat);

    int deleteGroupChatByIds(Long[] groupIds);
}
