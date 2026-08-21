package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizGroupChat;
import com.ruoyi.biz.mapper.BizGroupChatMapper;
import com.ruoyi.biz.service.IBizGroupChatService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizGroupChatServiceImpl implements IBizGroupChatService
{
    @Autowired
    private BizGroupChatMapper groupChatMapper;

    @Override
    public BizGroupChat selectGroupChatById(Long groupId)
    {
        return groupChatMapper.selectGroupChatById(groupId);
    }

    @Override
    public List<BizGroupChat> selectGroupChatList(BizGroupChat groupChat)
    {
        return groupChatMapper.selectGroupChatList(groupChat);
    }

    @Override
    public List<BizGroupChat> selectAppGroupChatList()
    {
        BizGroupChat query = new BizGroupChat();
        query.setStatus(BizConstants.STATUS_OK);
        return groupChatMapper.selectGroupChatList(query);
    }

    @Override
    public int insertGroupChat(BizGroupChat groupChat)
    {
        fillDefaults(groupChat);
        checkRequired(groupChat);
        return groupChatMapper.insertGroupChat(groupChat);
    }

    @Override
    public int updateGroupChat(BizGroupChat groupChat)
    {
        fillDefaults(groupChat);
        checkRequired(groupChat);
        return groupChatMapper.updateGroupChat(groupChat);
    }

    @Override
    public int deleteGroupChatByIds(Long[] groupIds)
    {
        return groupChatMapper.deleteGroupChatByIds(groupIds);
    }

    private void fillDefaults(BizGroupChat groupChat)
    {
        if (StringUtils.isEmpty(groupChat.getStatus()))
        {
            groupChat.setStatus(BizConstants.STATUS_OK);
        }
        if (StringUtils.isEmpty(groupChat.getHint()))
        {
            groupChat.setHint("扫码进群");
        }
        if (groupChat.getQrUrl() == null)
        {
            groupChat.setQrUrl("");
        }
        if (groupChat.getRemark() == null)
        {
            groupChat.setRemark("");
        }
        if (groupChat.getSort() == null)
        {
            groupChat.setSort(0);
        }
    }

    private void checkRequired(BizGroupChat groupChat)
    {
        if (StringUtils.isEmpty(groupChat.getTitle()))
        {
            throw new ServiceException("请填写标题");
        }
        if (StringUtils.isEmpty(groupChat.getQrUrl()))
        {
            throw new ServiceException("请上传群聊二维码");
        }
    }
}
