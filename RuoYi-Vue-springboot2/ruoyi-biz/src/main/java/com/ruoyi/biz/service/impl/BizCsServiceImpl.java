package com.ruoyi.biz.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.api.AppCsChannelItem;
import com.ruoyi.biz.api.AppServiceData;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizCsChannel;
import com.ruoyi.biz.domain.BizCsConfig;
import com.ruoyi.biz.mapper.BizCsChannelMapper;
import com.ruoyi.biz.service.IBizCsService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class BizCsServiceImpl implements IBizCsService
{
    @Autowired
    private BizCsChannelMapper channelMapper;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Override
    public BizCsConfig getConfig()
    {
        BizCsConfig config = new BizCsConfig();
        config.setTitle(strVal(BizConstants.CONFIG_SERVICE_TITLE, "客服中心"));
        config.setWorkTime(strVal(BizConstants.CONFIG_SERVICE_WORK_TIME, "09:00 - 21:00"));
        config.setHint(strVal(BizConstants.CONFIG_SERVICE_HINT, "通道拥堵可联系在线客服"));
        config.setChannels(channelMapper.selectChannelList(new BizCsChannel()));
        return config;
    }

    @Override
    public void saveConfig(BizCsConfig config)
    {
        if (config == null)
        {
            throw new ServiceException("请填写配置");
        }
        saveCfg(BizConstants.CONFIG_SERVICE_TITLE, "客服中心标题",
                StringUtils.isEmpty(config.getTitle()) ? "客服中心" : config.getTitle(), "App客服中心标题");
        saveCfg(BizConstants.CONFIG_SERVICE_WORK_TIME, "客服工作时间",
                StringUtils.isEmpty(config.getWorkTime()) ? "09:00 - 21:00" : config.getWorkTime(), "App客服工作时间");
        saveCfg(BizConstants.CONFIG_SERVICE_HINT, "客服提示文案",
                config.getHint() == null ? "" : config.getHint(), "App客服说明");
    }

    @Override
    public AppServiceData getAppService()
    {
        BizCsConfig config = getConfig();
        AppServiceData data = new AppServiceData();
        data.setTitle(config.getTitle());
        data.setWorkTime(config.getWorkTime());
        data.setHint(config.getHint());
        List<AppCsChannelItem> rows = new ArrayList<AppCsChannelItem>();
        List<BizCsChannel> channels = config.getChannels();
        for (int i = 0; i < channels.size(); i++)
        {
            BizCsChannel ch = channels.get(i);
            if (!BizConstants.STATUS_OK.equals(ch.getStatus()))
            {
                continue;
            }
            AppCsChannelItem item = new AppCsChannelItem();
            item.setChannelId(ch.getChannelId());
            item.setName(ch.getName());
            item.setType(ch.getChannelType());
            item.setValue(ch.getValue() == null ? "" : ch.getValue());
            item.setQrUrl(ch.getQrUrl() == null ? "" : ch.getQrUrl());
            item.setLinkUrl(ch.getLinkUrl() == null ? "" : ch.getLinkUrl());
            item.setSort(ch.getSort());
            rows.add(item);
        }
        data.setChannels(rows);
        return data;
    }

    @Override
    public BizCsChannel selectChannelById(Long channelId)
    {
        return channelMapper.selectChannelById(channelId);
    }

    @Override
    public List<BizCsChannel> selectChannelList(BizCsChannel query)
    {
        return channelMapper.selectChannelList(query);
    }

    @Override
    public int insertChannel(BizCsChannel channel)
    {
        fill(channel);
        if (StringUtils.isEmpty(channel.getName()))
        {
            throw new ServiceException("请填写名称");
        }
        return channelMapper.insertChannel(channel);
    }

    @Override
    public int updateChannel(BizCsChannel channel)
    {
        fill(channel);
        return channelMapper.updateChannel(channel);
    }

    @Override
    public int deleteChannelByIds(Long[] channelIds)
    {
        return channelMapper.deleteChannelByIds(channelIds);
    }

    private void fill(BizCsChannel channel)
    {
        if (StringUtils.isEmpty(channel.getChannelType()))
        {
            channel.setChannelType("WECHAT");
        }
        else
        {
            channel.setChannelType(channel.getChannelType().toUpperCase());
        }
        if (channel.getValue() == null)
        {
            channel.setValue("");
        }
        if (channel.getQrUrl() == null)
        {
            channel.setQrUrl("");
        }
        if (channel.getLinkUrl() == null)
        {
            channel.setLinkUrl("");
        }
        if (channel.getSort() == null)
        {
            channel.setSort(Integer.valueOf(0));
        }
        if (StringUtils.isEmpty(channel.getStatus()))
        {
            channel.setStatus(BizConstants.STATUS_OK);
        }
    }

    private void saveCfg(String key, String name, String value, String remark)
    {
        SysConfig existing = sysConfigMapper.checkConfigKeyUnique(key);
        if (existing == null)
        {
            SysConfig config = new SysConfig();
            config.setConfigName(name);
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("N");
            config.setRemark(remark);
            sysConfigService.insertConfig(config);
        }
        else
        {
            existing.setConfigName(name);
            existing.setConfigValue(value);
            existing.setRemark(remark);
            sysConfigService.updateConfig(existing);
        }
    }

    private String strVal(String key, String defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }
}
