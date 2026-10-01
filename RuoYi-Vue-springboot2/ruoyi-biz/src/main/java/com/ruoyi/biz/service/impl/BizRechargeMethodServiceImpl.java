package com.ruoyi.biz.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.api.AppPayChannelItem;
import com.ruoyi.biz.api.AppRechargeMethodItem;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizPayChannel;
import com.ruoyi.biz.domain.BizRechargeMethod;
import com.ruoyi.biz.mapper.BizPayChannelMapper;
import com.ruoyi.biz.mapper.BizRechargeMethodMapper;
import com.ruoyi.biz.service.IBizRechargeMethodService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizRechargeMethodServiceImpl implements IBizRechargeMethodService
{
    @Autowired
    private BizRechargeMethodMapper rechargeMethodMapper;

    @Autowired
    private BizPayChannelMapper payChannelMapper;

    @Override
    public BizRechargeMethod selectRechargeMethodById(Long methodId)
    {
        return rechargeMethodMapper.selectRechargeMethodById(methodId);
    }

    @Override
    public List<BizRechargeMethod> selectRechargeMethodList(BizRechargeMethod query)
    {
        return rechargeMethodMapper.selectRechargeMethodList(query == null ? new BizRechargeMethod() : query);
    }

    @Override
    public int insertRechargeMethod(BizRechargeMethod row)
    {
        normalize(row);
        assertValid(row, true);
        return rechargeMethodMapper.insertRechargeMethod(row);
    }

    @Override
    public int updateRechargeMethod(BizRechargeMethod row)
    {
        if (row == null || row.getMethodId() == null)
        {
            throw new ServiceException("缺少方式ID");
        }
        normalize(row);
        assertValid(row, false);
        return rechargeMethodMapper.updateRechargeMethod(row);
    }

    @Override
    public int deleteRechargeMethodByIds(Long[] methodIds)
    {
        if (methodIds == null || methodIds.length == 0)
        {
            throw new ServiceException("请选择要删除的方式");
        }
        for (int i = 0; i < methodIds.length; i++)
        {
            BizRechargeMethod method = rechargeMethodMapper.selectRechargeMethodById(methodIds[i]);
            if (method == null || StringUtils.isEmpty(method.getMethodCode()))
            {
                continue;
            }
            BizPayChannel q = new BizPayChannel();
            q.setScene(method.getMethodCode());
            List<BizPayChannel> bound = payChannelMapper.selectPayChannelList(q);
            if (bound != null && !bound.isEmpty())
            {
                throw new ServiceException("支付方式下仍有支付通道，请先改绑或删除通道");
            }
        }
        return rechargeMethodMapper.deleteRechargeMethodByIds(methodIds);
    }

    @Override
    public List<AppRechargeMethodItem> listAppMethods()
    {
        BizRechargeMethod query = new BizRechargeMethod();
        query.setStatus(BizConstants.STATUS_OK);
        List<BizRechargeMethod> rows = rechargeMethodMapper.selectRechargeMethodList(query);
        List<AppRechargeMethodItem> list = new ArrayList<AppRechargeMethodItem>();
        for (int i = 0; i < rows.size(); i++)
        {
            BizRechargeMethod row = rows.get(i);
            AppRechargeMethodItem item = new AppRechargeMethodItem();
            item.setMethodId(row.getMethodId());
            item.setMethodCode(row.getMethodCode());
            item.setLabel(row.getLabel());
            item.setIconUrl(row.getIconUrl() == null ? "" : row.getIconUrl());
            item.setIsCs(Boolean.valueOf("1".equals(row.getIsCs())));
            item.setSortOrder(row.getSortOrder());
            list.add(item);
        }
        return list;
    }

    @Override
    public List<AppPayChannelItem> listAppChannelsByMethodCode(String methodCode)
    {
        if (StringUtils.isEmpty(methodCode))
        {
            throw new ServiceException("缺少支付方式");
        }
        String code = methodCode.trim().toLowerCase();
        BizRechargeMethod method = rechargeMethodMapper.selectByMethodCode(code);
        if (method == null || !BizConstants.STATUS_OK.equals(method.getStatus()))
        {
            throw new ServiceException("支付方式不可用");
        }
        if ("1".equals(method.getIsCs()))
        {
            return new ArrayList<AppPayChannelItem>();
        }
        BizPayChannel cq = new BizPayChannel();
        cq.setScene(code);
        List<BizPayChannel> channelRows = payChannelMapper.selectOperationalChannels(cq);
        List<AppPayChannelItem> channels = new ArrayList<AppPayChannelItem>();
        for (int j = 0; j < channelRows.size(); j++)
        {
            BizPayChannel ch = channelRows.get(j);
            String fulfill = ch.getFulfillType() == null ? BizConstants.PAY_FULFILL_ONLINE
                    : ch.getFulfillType().trim().toUpperCase();
            AppPayChannelItem item = new AppPayChannelItem();
            item.setChannelCode(ch.getChannelCode());
            String name = StringUtils.isEmpty(ch.getDisplayName()) ? ch.getChannelName() : ch.getDisplayName();
            item.setName(name);
            item.setScene(ch.getScene());
            item.setFulfillType(fulfill);
            item.setProviderCode(ch.getProviderCode());
            item.setProviderName(ch.getProviderName());
            item.setCurrency(ch.getCurrency());
            item.setMinAmount(ch.getMinAmount());
            item.setMaxAmount(ch.getMaxAmount());
            item.setMock(BizConstants.PAY_MOCK_YES.equals(ch.getMockMode()));
            channels.add(item);
        }
        return channels;
    }

    private void normalize(BizRechargeMethod row)
    {
        if (row == null)
        {
            throw new ServiceException("请填写充值方式");
        }
        if (row.getMethodCode() != null)
        {
            row.setMethodCode(row.getMethodCode().trim().toLowerCase());
        }
        if (row.getLabel() != null)
        {
            row.setLabel(row.getLabel().trim());
        }
        if (row.getIconUrl() == null)
        {
            row.setIconUrl("");
        }
        else
        {
            row.setIconUrl(row.getIconUrl().trim());
        }
        row.setIsCs("1".equals(row.getIsCs()) ? "1" : "0");
        if (StringUtils.isEmpty(row.getStatus()))
        {
            row.setStatus(BizConstants.STATUS_OK);
        }
        if (row.getSortOrder() == null)
        {
            row.setSortOrder(Integer.valueOf(0));
        }
    }

    private void assertValid(BizRechargeMethod row, boolean creating)
    {
        if (StringUtils.isEmpty(row.getMethodCode()))
        {
            throw new ServiceException("请填写编码");
        }
        if (StringUtils.isEmpty(row.getLabel()))
        {
            throw new ServiceException("请填写展示名");
        }
        BizRechargeMethod exists = rechargeMethodMapper.selectByMethodCode(row.getMethodCode());
        if (exists != null && (creating || !exists.getMethodId().equals(row.getMethodId())))
        {
            throw new ServiceException("编码已存在：" + row.getMethodCode());
        }
    }
}
