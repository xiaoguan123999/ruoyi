package com.ruoyi.biz.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.biz.api.AppPayChannelItem;
import com.ruoyi.biz.api.AppPayDepositData;
import com.ruoyi.biz.domain.BizPayChannel;
import com.ruoyi.biz.domain.BizPayOrder;
import com.ruoyi.biz.domain.BizPayProvider;

public interface IBizOnlinePayService
{
    List<BizPayProvider> selectProviderList(BizPayProvider query);

    BizPayProvider selectProviderById(Long providerId);

    int updateProvider(BizPayProvider row);

    List<BizPayChannel> selectChannelList(BizPayChannel query);

    BizPayChannel selectChannelById(Long channelId);

    int updateChannel(BizPayChannel row);

    List<AppPayChannelItem> listAppChannels(String scene);

    AppPayDepositData createDeposit(Long memberId, java.math.BigDecimal amount, String scene,
            String channelCode, String baseUrl, String clientIp, String returnUrl);

    List<BizPayOrder> selectPayOrderList(BizPayOrder query);

    BizPayOrder selectPayOrderByOutTradeNo(String outTradeNo);

    String handleNotify(String providerCode, Map<String, String> payload, String rawBody);

    void simulatePaid(String outTradeNo, String operator);

    String mockCashierHtml(String outTradeNo);
}
