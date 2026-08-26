package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.api.AppPayChannelItem;
import com.ruoyi.biz.api.AppPayDepositData;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizPayChannel;
import com.ruoyi.biz.domain.BizPayOrder;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.biz.domain.BizRecharge;
import com.ruoyi.biz.mapper.BizPayChannelMapper;
import com.ruoyi.biz.mapper.BizPayOrderMapper;
import com.ruoyi.biz.mapper.BizPayProviderMapper;
import com.ruoyi.biz.pay.BizPayAdapterFactory;
import com.ruoyi.biz.pay.IBizPayAdapter;
import com.ruoyi.biz.pay.MonPaySign;
import com.ruoyi.biz.pay.PayCreateRequest;
import com.ruoyi.biz.pay.PayCreateResult;
import com.ruoyi.biz.service.IBizOnlinePayService;
import com.ruoyi.biz.service.IBizRechargeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.html.EscapeUtil;

@Service
public class BizOnlinePayServiceImpl implements IBizOnlinePayService
{
    @Autowired
    private BizPayProviderMapper providerMapper;

    @Autowired
    private BizPayChannelMapper channelMapper;

    @Autowired
    private BizPayOrderMapper payOrderMapper;

    @Autowired
    private IBizRechargeService rechargeService;

    @Autowired
    private BizPayAdapterFactory adapterFactory;

    @Override
    public List<BizPayProvider> selectProviderList(BizPayProvider query)
    {
        return providerMapper.selectPayProviderList(query == null ? new BizPayProvider() : query);
    }

    @Override
    public BizPayProvider selectProviderById(Long providerId)
    {
        if (providerId == null)
        {
            return null;
        }
        return providerMapper.selectPayProviderById(providerId);
    }

    @Override
    public int updateProvider(BizPayProvider row)
    {
        if (row == null || row.getProviderId() == null)
        {
            throw new ServiceException("服务商不存在");
        }
        if (providerMapper.selectPayProviderById(row.getProviderId()) == null)
        {
            throw new ServiceException("服务商不存在");
        }
        if (StringUtils.isEmpty(row.getSecretKey()))
        {
            row.setSecretKey(null);
        }
        return providerMapper.updatePayProvider(row);
    }

    @Override
    public List<BizPayChannel> selectChannelList(BizPayChannel query)
    {
        return channelMapper.selectPayChannelList(query == null ? new BizPayChannel() : query);
    }

    @Override
    public BizPayChannel selectChannelById(Long channelId)
    {
        return channelMapper.selectPayChannelById(channelId);
    }

    @Override
    public int updateChannel(BizPayChannel row)
    {
        if (row == null || row.getChannelId() == null)
        {
            throw new ServiceException("通道不存在");
        }
        return channelMapper.updatePayChannel(row);
    }

    @Override
    public List<AppPayChannelItem> listAppChannels(String scene)
    {
        BizPayChannel query = new BizPayChannel();
        if (StringUtils.isNotEmpty(scene))
        {
            query.setScene(scene.trim().toLowerCase());
        }
        List<BizPayChannel> rows = channelMapper.selectOperationalChannels(query);
        List<AppPayChannelItem> list = new ArrayList<AppPayChannelItem>();
        if (rows == null)
        {
            return list;
        }
        for (BizPayChannel row : rows)
        {
            AppPayChannelItem item = new AppPayChannelItem();
            item.setChannelCode(row.getChannelCode());
            String name = StringUtils.isEmpty(row.getDisplayName()) ? row.getChannelName() : row.getDisplayName();
            item.setName(name);
            item.setScene(row.getScene());
            item.setProviderCode(row.getProviderCode());
            item.setProviderName(row.getProviderName());
            item.setCurrency(row.getCurrency());
            item.setMinAmount(row.getMinAmount());
            item.setMaxAmount(row.getMaxAmount());
            item.setMock(BizConstants.PAY_MOCK_YES.equals(row.getMockMode()));
            list.add(item);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPayDepositData createDeposit(Long memberId, BigDecimal amount, String scene, String channelCode,
            String baseUrl, String clientIp, String returnUrl)
    {
        if (memberId == null)
        {
            throw new ServiceException("请先登录");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("充值金额必须大于0");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        BizPayChannel channel = resolveChannel(scene, channelCode);
        if (channel.getMinAmount() != null && amount.compareTo(channel.getMinAmount()) < 0)
        {
            throw new ServiceException("最低充值 " + channel.getMinAmount().stripTrailingZeros().toPlainString());
        }
        if (channel.getMaxAmount() != null && amount.compareTo(channel.getMaxAmount()) > 0)
        {
            throw new ServiceException("最高充值 " + channel.getMaxAmount().stripTrailingZeros().toPlainString());
        }
        BizPayProvider provider = providerMapper.selectPayProviderByCode(channel.getProviderCode());
        if (provider == null || !BizConstants.STATUS_OK.equals(provider.getStatus()))
        {
            throw new ServiceException("支付通道暂不可用");
        }
        String outTradeNo = nextOutTradeNo(memberId);
        String display = StringUtils.isEmpty(channel.getDisplayName()) ? channel.getChannelName() : channel.getDisplayName();
        BizRecharge recharge = rechargeService.applyOnline(memberId, channel.getCurrency(), amount,
                display, channel.getChannelCode(), outTradeNo);
        Date expire = minutesLater(30);
        BizPayOrder order = new BizPayOrder();
        order.setOutTradeNo(outTradeNo);
        order.setRechargeId(recharge.getRechargeId());
        order.setMemberId(memberId);
        order.setProviderCode(provider.getProviderCode());
        order.setChannelCode(channel.getChannelCode());
        order.setProductId(channel.getProductId());
        order.setCurrency(channel.getCurrency());
        order.setAmount(amount);
        order.setProviderAmount(amount);
        order.setStatus(BizConstants.PAY_ORDER_WAIT);
        order.setExpireTime(expire);
        order.setRemark(display);
        payOrderMapper.insertPayOrder(order);

        String base = trimSlash(baseUrl);
        PayCreateRequest req = new PayCreateRequest();
        req.setOutTradeNo(outTradeNo);
        req.setProductId(channel.getProductId());
        req.setAmount(amount);
        req.setNotifyUrl(base + "/pay/callback/" + provider.getProviderCode() + "/deposit");
        req.setReturnUrl(StringUtils.isEmpty(returnUrl) ? base : returnUrl);
        req.setBaseUrl(base);
        req.setClientIp(clientIp);
        IBizPayAdapter adapter = adapterFactory.getAdapter(provider);
        PayCreateResult placed = adapter.createOrder(provider, req);
        if (placed == null || StringUtils.isEmpty(placed.getPayUrl()))
        {
            throw new ServiceException("拉单失败，请稍后再试");
        }
        order.setPayType(placed.getPayType());
        order.setPayUrl(placed.getPayUrl());
        order.setProviderTradeNo(placed.getProviderTradeNo());
        payOrderMapper.updatePayOrder(order);

        AppPayDepositData data = new AppPayDepositData();
        data.setOutTradeNo(outTradeNo);
        data.setRechargeId(recharge.getRechargeId());
        data.setPayUrl(placed.getPayUrl());
        data.setPayType(placed.getPayType());
        data.setAmount(amount);
        data.setCurrency(channel.getCurrency());
        data.setChannelCode(channel.getChannelCode());
        data.setChannelName(display);
        data.setProviderCode(provider.getProviderCode());
        data.setMock(BizConstants.PAY_MOCK_YES.equals(provider.getMockMode()));
        data.setExpireTime(formatTime(expire));
        return data;
    }

    @Override
    public List<BizPayOrder> selectPayOrderList(BizPayOrder query)
    {
        return payOrderMapper.selectPayOrderList(query == null ? new BizPayOrder() : query);
    }

    @Override
    public BizPayOrder selectPayOrderByOutTradeNo(String outTradeNo)
    {
        return payOrderMapper.selectPayOrderByOutTradeNo(outTradeNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(String providerCode, Map<String, String> payload, String rawBody)
    {
        if (StringUtils.isEmpty(providerCode))
        {
            throw new ServiceException("缺少服务商");
        }
        BizPayProvider provider = providerMapper.selectPayProviderByCode(providerCode);
        if (provider == null)
        {
            throw new ServiceException("未知服务商");
        }
        IBizPayAdapter adapter = adapterFactory.getAdapter(provider);
        if (!adapter.verifyNotify(provider, payload))
        {
            throw new ServiceException("验签失败");
        }
        String outTradeNo = first(payload, "out_trade_no", "outTradeNo");
        if (StringUtils.isEmpty(outTradeNo))
        {
            throw new ServiceException("缺少商户单号");
        }
        BizPayOrder locked = payOrderMapper.selectPayOrderByOutTradeNoForUpdate(outTradeNo);
        if (locked == null)
        {
            throw new ServiceException("支付单不存在");
        }
        if (!providerCode.equals(locked.getProviderCode()))
        {
            throw new ServiceException("服务商不匹配");
        }
        if (BizConstants.PAY_ORDER_SUCCESS.equals(locked.getStatus()))
        {
            return "success";
        }
        if (!adapter.isPaid(payload))
        {
            locked.setNotifyPayload(cut(rawBody, 1800));
            locked.setStatus(BizConstants.PAY_ORDER_FAIL);
            payOrderMapper.updatePayOrder(locked);
            return "success";
        }
        locked.setStatus(BizConstants.PAY_ORDER_SUCCESS);
        locked.setPaidTime(new Date());
        locked.setNotifyPayload(cut(rawBody, 1800));
        String tradeNo = first(payload, "trade_no", "tradeNo");
        if (StringUtils.isNotEmpty(tradeNo))
        {
            locked.setProviderTradeNo(tradeNo);
        }
        payOrderMapper.updatePayOrder(locked);
        rechargeService.audit(locked.getRechargeId(), BizConstants.AUDIT_PASS, "system", "线上支付自动到账");
        return "success";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatePaid(String outTradeNo, String operator)
    {
        BizPayOrder order = payOrderMapper.selectPayOrderByOutTradeNo(outTradeNo);
        if (order == null)
        {
            throw new ServiceException("支付单不存在");
        }
        BizPayProvider provider = providerMapper.selectPayProviderByCode(order.getProviderCode());
        if (provider == null || !BizConstants.PAY_MOCK_YES.equals(provider.getMockMode()))
        {
            throw new ServiceException("仅模拟通道可点到账");
        }
        if (BizConstants.PAY_ORDER_SUCCESS.equals(order.getStatus()))
        {
            return;
        }
        Map<String, String> payload = new LinkedHashMap<String, String>();
        payload.put("app_id", provider.getAppId());
        payload.put("out_trade_no", order.getOutTradeNo());
        payload.put("trade_no", "SIM" + order.getOutTradeNo());
        payload.put("amount", order.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        payload.put("trade_status", BizConstants.PAY_TRADE_SUCCESS);
        payload.put("time", String.valueOf(System.currentTimeMillis() / 1000L));
        payload.put("sign", MonPaySign.sign(payload, provider.getSecretKey()));
        handleNotify(provider.getProviderCode(), payload, "simulate by " + operator);
    }

    @Override
    public String mockCashierHtml(String outTradeNo)
    {
        BizPayOrder order = payOrderMapper.selectPayOrderByOutTradeNo(outTradeNo);
        if (order == null)
        {
            return page("模拟收银台", "<p>订单不存在</p>");
        }
        if (BizConstants.PAY_ORDER_SUCCESS.equals(order.getStatus()))
        {
            return page("支付成功", "<p>该笔已到账，可返回 App 查看余额。</p>");
        }
        String name = StringUtils.isEmpty(order.getChannelName()) ? order.getChannelCode() : order.getChannelName();
        String amount = order.getAmount() == null ? "0.00" : order.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String safeNo = EscapeUtil.escape(order.getOutTradeNo());
        String body = "<p>服务商：" + EscapeUtil.escape(nvl(order.getProviderName(), order.getProviderCode()))
                + "</p><p>通道：" + EscapeUtil.escape(name)
                + "</p><p>金额：<b>" + EscapeUtil.escape(amount) + " " + EscapeUtil.escape(order.getCurrency())
                + "</b></p><p>单号：" + safeNo
                + "</p><form method='post' action='/pay/mock/pay'>"
                + "<input type='hidden' name='outTradeNo' value='" + safeNo + "'/>"
                + "<button type='submit'>模拟支付成功</button></form>"
                + "<p style='color:#888;margin-top:16px'>当前是模拟收银台，正式环境会跳转到百付/宝利/牛付/沙付。</p>";
        return page("模拟收银台", body);
    }

    private BizPayChannel resolveChannel(String scene, String channelCode)
    {
        if (StringUtils.isNotEmpty(channelCode))
        {
            BizPayChannel hit = channelMapper.selectPayChannelByCode(channelCode.trim());
            if (hit == null || !BizConstants.STATUS_OK.equals(hit.getStatus())
                    || !BizConstants.STATUS_OK.equals(hit.getProviderStatus()))
            {
                throw new ServiceException("支付通道不可用");
            }
            return hit;
        }
        BizPayChannel query = new BizPayChannel();
        query.setScene(StringUtils.isEmpty(scene) ? BizConstants.PAY_SCENE_ALIPAY : scene.trim().toLowerCase());
        List<BizPayChannel> list = channelMapper.selectOperationalChannels(query);
        if (list == null || list.isEmpty())
        {
            throw new ServiceException("暂无可用支付通道");
        }
        return list.get(0);
    }

    private static String nextOutTradeNo(Long memberId)
    {
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        long tail = memberId == null ? 0L : memberId.longValue() % 10000L;
        return "P" + time + String.format("%04d", Long.valueOf(tail));
    }

    private static Date minutesLater(int minutes)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    private static String formatTime(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private static String trimSlash(String base)
    {
        if (base == null)
        {
            return "";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private static String first(Map<String, String> payload, String a, String b)
    {
        if (payload == null)
        {
            return null;
        }
        String v = payload.get(a);
        if (StringUtils.isEmpty(v))
        {
            v = payload.get(b);
        }
        return v;
    }

    private static String cut(String raw, int max)
    {
        if (raw == null)
        {
            return "";
        }
        return raw.length() <= max ? raw : raw.substring(0, max);
    }

    private static String nvl(String v, String fallback)
    {
        return StringUtils.isEmpty(v) ? fallback : v;
    }

    private static String page(String title, String body)
    {
        return "<!doctype html><html><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width,initial-scale=1'/>"
                + "<title>" + title + "</title><style>body{font-family:-apple-system,sans-serif;padding:24px;max-width:420px;margin:auto}"
                + "button{width:100%;padding:12px;font-size:16px;background:#1677ff;color:#fff;border:0;border-radius:8px}</style></head><body>"
                + "<h3>" + title + "</h3>" + body + "</body></html>";
    }
}
