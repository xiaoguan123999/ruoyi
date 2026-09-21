package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.api.AppChainDepositConfigData;
import com.ruoyi.biz.api.AppChainDepositNetworkItem;
import com.ruoyi.biz.api.AppChainDepositOrderData;
import com.ruoyi.biz.chain.Bep20Address;
import com.ruoyi.biz.chain.BscScanClient;
import com.ruoyi.biz.chain.ChainCollectTarget;
import com.ruoyi.biz.chain.ChainNetwork;
import com.ruoyi.biz.chain.TronAddress;
import com.ruoyi.biz.chain.TronGridClient;
import com.ruoyi.biz.chain.TronUsdtTransfer;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizChainDeposit;
import com.ruoyi.biz.domain.BizChainDepositConfig;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizRecharge;
import com.ruoyi.biz.mapper.BizChainDepositMapper;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.service.IBizChainDepositService;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizRechargeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

@Service
public class BizChainDepositServiceImpl implements IBizChainDepositService
{
    private static final Logger log = LoggerFactory.getLogger(BizChainDepositServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int FINGERPRINT_TRIES = 50;
    private static final String DEFAULT_HINT =
            "请向该地址转入所选网络的 USDT，金额必须与显示的6位小数完全一致。请勿转 TRX、BNB 或其他币。超时未到账请重新下单。";

    @Autowired
    private BizChainDepositMapper depositMapper;

    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private IBizRechargeService rechargeService;

    @Autowired
    private IBizConfigService bizConfigService;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private TronGridClient tronGridClient;

    @Autowired
    private BscScanClient bscScanClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public AppChainDepositConfigData getAppConfig(Long memberId)
    {
        boolean usdtOn = bizConfigService.isUsdtEnabled();
        ChainCollectTarget trc = resolveCollect(memberId, BizConstants.CHAIN_NETWORK_TRC20);
        ChainCollectTarget bep = resolveCollect(memberId, BizConstants.CHAIN_NETWORK_BEP20);
        boolean trcOn = usdtOn && bool(BizConstants.CONFIG_CHAIN_ENABLED, true) && StringUtils.isNotEmpty(trc.getAddress());
        boolean bepOn = usdtOn && bool(BizConstants.CONFIG_CHAIN_BSC_ENABLED, true) && StringUtils.isNotEmpty(bep.getAddress());

        AppChainDepositConfigData data = new AppChainDepositConfigData();
        data.setEnabled(Boolean.valueOf(trcOn || bepOn));
        data.setCurrency(BizConstants.CURRENCY_USDT);
        data.setExpireMinutes(Integer.valueOf(expireMinutes()));
        data.setMinAmount(minAmount());
        data.setMaxAmount(maxAmount());
        data.setHint(hint());

        List<AppChainDepositNetworkItem> networks = new ArrayList<AppChainDepositNetworkItem>();
        networks.add(networkItem(BizConstants.CHAIN_NETWORK_TRC20, "TRC20", BizConstants.CHAIN_ASSET_USDT_TRC20,
                trc, BizConstants.USDT_TRC20_CONTRACT, 6, trcOn));
        networks.add(networkItem(BizConstants.CHAIN_NETWORK_BEP20, "BEP20", BizConstants.CHAIN_ASSET_USDT_BEP20,
                bep, BizConstants.USDT_BEP20_CONTRACT, 18, bepOn));
        data.setNetworks(networks);

        AppChainDepositNetworkItem def = trcOn ? networks.get(0) : (bepOn ? networks.get(1) : networks.get(0));
        data.setNetwork(def.getNetwork());
        data.setAsset(def.getAsset());
        data.setAddress(Boolean.TRUE.equals(def.getEnabled()) ? def.getAddress() : "");
        data.setAddressSource(Boolean.TRUE.equals(def.getEnabled()) ? def.getAddressSource() : "");
        data.setContractAddress(def.getContractAddress());
        data.setDecimals(def.getDecimals());
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppChainDepositOrderData create(Long memberId, BigDecimal amount, String networkRaw)
    {
        bizConfigService.assertCurrencyEnabled(BizConstants.CURRENCY_USDT);
        String network = ChainNetwork.normalize(networkRaw);
        boolean chainOn = ChainNetwork.isBep20(network)
                ? bool(BizConstants.CONFIG_CHAIN_BSC_ENABLED, true)
                : bool(BizConstants.CONFIG_CHAIN_ENABLED, true);
        if (!chainOn)
        {
            throw new ServiceException("链上充值暂未开放");
        }
        ChainCollectTarget target = resolveCollect(memberId, network);
        if (StringUtils.isEmpty(target.getAddress()))
        {
            throw new ServiceException("暂未配置收款地址");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("充值金额必须大于0");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal min = minAmount();
        if (min != null && amount.compareTo(min) < 0)
        {
            throw new ServiceException("最低充值 " + min.stripTrailingZeros().toPlainString());
        }
        BigDecimal max = maxAmount();
        if (max != null && amount.compareTo(max) > 0)
        {
            throw new ServiceException("最高充值 " + max.stripTrailingZeros().toPlainString());
        }
        BigDecimal payAmount = nextFingerprint(amount, target.getAddress(), network);
        String outTradeNo = nextOutTradeNo(memberId);
        String asset = ChainNetwork.isBep20(network) ? BizConstants.CHAIN_ASSET_USDT_BEP20 : BizConstants.CHAIN_ASSET_USDT_TRC20;
        String channel = ChainNetwork.isBep20(network) ? BizConstants.CHAIN_CHANNEL_USDT_BEP20 : BizConstants.CHAIN_CHANNEL_USDT;
        BizRecharge recharge = rechargeService.applyOnline(memberId, BizConstants.CURRENCY_USDT, payAmount,
                asset, channel, outTradeNo);
        Date expire = minutesLater(expireMinutes());
        BizChainDeposit row = new BizChainDeposit();
        row.setOutTradeNo(outTradeNo);
        row.setRechargeId(recharge.getRechargeId());
        row.setMemberId(memberId);
        row.setAsset(asset);
        row.setNetwork(network);
        row.setCurrency(BizConstants.CURRENCY_USDT);
        row.setAmount(amount);
        row.setPayAmount(payAmount);
        row.setAddress(target.getAddress());
        row.setAddressSource(target.getSource());
        row.setCollectMemberId(target.getCollectMemberId());
        row.setStatus(BizConstants.CHAIN_STATUS_WAIT);
        row.setExpireTime(expire);
        row.setRemark("fingerprint");
        depositMapper.insertDeposit(row);
        return toApp(row);
    }

    @Override
    public AppChainDepositOrderData getOrder(Long memberId, String outTradeNo)
    {
        if (StringUtils.isEmpty(outTradeNo))
        {
            throw new ServiceException("缺少单号");
        }
        proxy().expirePending();
        BizChainDeposit row = depositMapper.selectByOutTradeNo(outTradeNo);
        if (row == null || row.getMemberId() == null || !row.getMemberId().equals(memberId))
        {
            throw new ServiceException("充值单不存在");
        }
        return toApp(row);
    }

    @Override
    public List<BizChainDeposit> selectList(BizChainDeposit query)
    {
        return depositMapper.selectList(query == null ? new BizChainDeposit() : query);
    }

    @Override
    public void scan()
    {
        IBizChainDepositService self = proxy();
        self.expirePending();
        if (bool(BizConstants.CONFIG_CHAIN_MOCK, false))
        {
            log.info("chain deposit scan skip, mock=1");
            return;
        }
        List<BizChainDeposit> collects = depositMapper.selectPendingCollects();
        if (collects == null || collects.isEmpty())
        {
            return;
        }
        String tronKey = config(BizConstants.CONFIG_CHAIN_API_KEY, "");
        String bscKey = config(BizConstants.CONFIG_CHAIN_BSC_API_KEY, "");
        String bscUrl = config(BizConstants.CONFIG_CHAIN_BSC_API_URL, "");
        for (int a = 0; a < collects.size(); a++)
        {
            BizChainDeposit collect = collects.get(a);
            String address = collect.getAddress();
            if (StringUtils.isEmpty(address))
            {
                continue;
            }
            String network = ChainNetwork.normalizeOrDefault(collect.getNetwork());
            Date since = depositMapper.selectMinPendingCreateTimeByAddress(network, address);
            List<TronUsdtTransfer> txs;
            try
            {
                if (ChainNetwork.isBep20(network))
                {
                    txs = bscScanClient.listIncomingUsdt(address, bscKey, bscUrl, since);
                }
                else
                {
                    txs = tronGridClient.listIncomingUsdt(address, tronKey, since);
                }
            }
            catch (Exception e)
            {
                log.warn("chain deposit scan fail network={} address={} msg={}", network, mask(address), e.getMessage());
                continue;
            }
            for (int i = 0; i < txs.size(); i++)
            {
                TronUsdtTransfer tx = txs.get(i);
                if (StringUtils.isEmpty(tx.getNetwork()))
                {
                    tx.setNetwork(network);
                }
                try
                {
                    self.creditByFingerprint(tx);
                }
                catch (Exception e)
                {
                    log.warn("chain deposit match fail tx={} msg={}", tx.getTxHash(), e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expirePending()
    {
        List<BizChainDeposit> list = depositMapper.selectExpiredPending();
        if (list == null || list.isEmpty())
        {
            return;
        }
        for (int i = 0; i < list.size(); i++)
        {
            BizChainDeposit row = list.get(i);
            row.setStatus(BizConstants.CHAIN_STATUS_EXPIRED);
            row.setRemark("expired");
            depositMapper.updateDeposit(row);
            rejectRecharge(row.getRechargeId(), "expired");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void creditByFingerprint(TronUsdtTransfer tx)
    {
        if (tx == null || StringUtils.isEmpty(tx.getTxHash()) || tx.getAmount() == null
                || StringUtils.isEmpty(tx.getToAddress()))
        {
            return;
        }
        if (depositMapper.selectByTxHash(tx.getTxHash()) != null)
        {
            return;
        }
        String network = ChainNetwork.normalizeOrDefault(tx.getNetwork());
        BigDecimal payAmount = tx.getAmount().setScale(6, RoundingMode.DOWN);
        BizChainDeposit locked = depositMapper.selectPendingByPayAmountForUpdate(payAmount, tx.getToAddress().trim(),
                network);
        if (locked == null)
        {
            return;
        }
        markPaid(locked, tx.getTxHash(), tx.getFromAddress(), "chain");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatePaid(String outTradeNo, String operator)
    {
        if (!bool(BizConstants.CONFIG_CHAIN_MOCK, false))
        {
            throw new ServiceException("仅模拟模式可点到账，请把 biz.chain.tron.mock 设为 1");
        }
        if (StringUtils.isEmpty(outTradeNo))
        {
            throw new ServiceException("缺少单号");
        }
        BizChainDeposit locked = depositMapper.selectByOutTradeNoForUpdate(outTradeNo);
        if (locked == null)
        {
            throw new ServiceException("充值单不存在");
        }
        if (BizConstants.CHAIN_STATUS_SUCCESS.equals(locked.getStatus()))
        {
            return;
        }
        if (!BizConstants.CHAIN_STATUS_WAIT.equals(locked.getStatus()))
        {
            throw new ServiceException("该单已关闭");
        }
        markPaid(locked, "SIM" + locked.getOutTradeNo(), "simulate",
                StringUtils.isEmpty(operator) ? "simulate" : operator);
    }


    @Override
    public BizChainDepositConfig getAdminConfig()
    {
        BizChainDepositConfig data = new BizChainDepositConfig();
        data.setTronEnabled(Boolean.valueOf(bool(BizConstants.CONFIG_CHAIN_ENABLED, true)));
        data.setTronAddress(config(BizConstants.CONFIG_CHAIN_ADDRESS, ""));
        data.setTronApiKey(config(BizConstants.CONFIG_CHAIN_API_KEY, ""));
        data.setBscEnabled(Boolean.valueOf(bool(BizConstants.CONFIG_CHAIN_BSC_ENABLED, true)));
        data.setBscAddress(config(BizConstants.CONFIG_CHAIN_BSC_ADDRESS, ""));
        data.setBscApiKey(config(BizConstants.CONFIG_CHAIN_BSC_API_KEY, ""));
        data.setBscApiUrl(config(BizConstants.CONFIG_CHAIN_BSC_API_URL, ""));
        data.setExpireMinutes(Integer.valueOf(expireMinutes()));
        data.setMinAmount(minAmount());
        data.setMaxAmount(maxAmount());
        data.setMock(Boolean.valueOf(bool(BizConstants.CONFIG_CHAIN_MOCK, false)));
        data.setHint(hint());
        return data;
    }

    @Override
    public void saveAdminConfig(BizChainDepositConfig cfg)
    {
        if (cfg == null)
        {
            throw new ServiceException("缺少配置");
        }
        String tronAddress = TronAddress.requireValidOrEmpty(cfg.getTronAddress());
        String bscAddress = Bep20Address.requireValidOrEmpty(cfg.getBscAddress());
        int minutes = cfg.getExpireMinutes() == null ? expireMinutes() : cfg.getExpireMinutes().intValue();
        if (minutes < 5)
        {
            minutes = 5;
        }
        BigDecimal min = cfg.getMinAmount() == null ? minAmount() : cfg.getMinAmount();
        BigDecimal max = cfg.getMaxAmount() == null ? maxAmount() : cfg.getMaxAmount();
        boolean tronOn = cfg.getTronEnabled() == null ? true : cfg.getTronEnabled().booleanValue();
        boolean bscOn = cfg.getBscEnabled() == null ? true : cfg.getBscEnabled().booleanValue();
        boolean mock = cfg.getMock() != null && cfg.getMock().booleanValue();
        String hintText = cfg.getHint() == null ? hint() : cfg.getHint().trim();
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_ENABLED, "TRC20链上充值开关",
                tronOn ? "true" : "false", "false 时 App 隐藏 TRC20");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_ADDRESS, "TRC20系统收款地址",
                tronAddress, "无团队地址时使用");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_API_KEY, "TronGrid API Key",
                nvl(cfg.getTronApiKey()), "TRON-PRO-API-KEY");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_BSC_ENABLED, "BEP20链上充值开关",
                bscOn ? "true" : "false", "false 时 App 隐藏 BEP20");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_BSC_ADDRESS, "BEP20系统收款地址",
                bscAddress, "无团队地址时使用");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_BSC_API_KEY, "BscScan API Key",
                nvl(cfg.getBscApiKey()), "BscScan / Etherscan tokentx");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_BSC_API_URL, "BscScan API URL",
                nvl(cfg.getBscApiUrl()), "empty = https://api.bscscan.com/api");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_EXPIRE_MINUTES, "链上充值超时分钟",
                String.valueOf(minutes), "pending fingerprint TTL minutes");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_MIN_AMOUNT, "链上充值最低USDT",
                min == null ? "10" : min.stripTrailingZeros().toPlainString(), "min requested amount");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_MAX_AMOUNT, "链上充值最高USDT",
                max == null ? "0" : max.stripTrailingZeros().toPlainString(), "0 unlimited");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_MOCK, "链上充值模拟",
                mock ? "1" : "0", "1 skip scan, admin simulate credit");
        bizConfigService.saveConfig(BizConstants.CONFIG_CHAIN_HINT, "链上充值提示",
                hintText, "shown on App pay screen");
        bizConfigService.refreshCache();
    }

    private static String nvl(String raw)
    {
        return raw == null ? "" : raw.trim();
    }

    /**
     * Nearest ancestor with the given network address wins; otherwise system default.
     * The member's own address is not used for himself.
     */
    private ChainCollectTarget resolveCollect(Long memberId, String network)
    {
        boolean bep20 = ChainNetwork.isBep20(network);
        if (memberId != null)
        {
            BizMember current = memberMapper.selectMemberCore(memberId);
            Long parentId = current == null ? null : current.getParentId();
            int guard = 0;
            while (parentId != null && parentId.longValue() > 0L && guard++ < 32)
            {
                BizMember parent = memberMapper.selectMemberCore(parentId);
                if (parent == null)
                {
                    break;
                }
                String teamAddress = bep20
                        ? Bep20Address.normalize(parent.getChainAddressBep20())
                        : TronAddress.normalize(parent.getChainAddress());
                if (StringUtils.isNotEmpty(teamAddress))
                {
                    return ChainCollectTarget.of(teamAddress, BizConstants.CHAIN_SOURCE_TEAM, parent.getMemberId());
                }
                parentId = parent.getParentId();
            }
        }
        String systemAddress = bep20
                ? Bep20Address.normalize(config(BizConstants.CONFIG_CHAIN_BSC_ADDRESS, ""))
                : TronAddress.normalize(config(BizConstants.CONFIG_CHAIN_ADDRESS, ""));
        return ChainCollectTarget.of(systemAddress, BizConstants.CHAIN_SOURCE_SYSTEM, null);
    }

    private void markPaid(BizChainDeposit locked, String txHash, String fromAddress, String operator)
    {
        locked.setStatus(BizConstants.CHAIN_STATUS_SUCCESS);
        locked.setTxHash(txHash);
        locked.setFromAddress(fromAddress == null ? "" : fromAddress);
        locked.setPaidTime(new Date());
        locked.setRemark(cut(operator, 80));
        depositMapper.updateDeposit(locked);
        rechargeService.audit(locked.getRechargeId(), BizConstants.AUDIT_PASS, operator,
                StringUtils.isEmpty(txHash) ? "chain deposit" : txHash);
    }

    private void rejectRecharge(Long rechargeId, String remark)
    {
        if (rechargeId == null)
        {
            return;
        }
        try
        {
            rechargeService.audit(rechargeId, BizConstants.AUDIT_REJECT, "chain", remark);
        }
        catch (ServiceException e)
        {
            log.debug("chain expire recharge {} {}", rechargeId, e.getMessage());
        }
    }

    private BigDecimal nextFingerprint(BigDecimal amount, String address, String network)
    {
        for (int i = 0; i < FINGERPRINT_TRIES; i++)
        {
            int micro = 1 + RANDOM.nextInt(9999);
            BigDecimal pay = amount.add(new BigDecimal(micro).movePointLeft(6)).setScale(6, RoundingMode.UNNECESSARY);
            if (depositMapper.countPendingByPayAmount(pay, address, network) == 0)
            {
                return pay;
            }
        }
        throw new ServiceException("匹配金额繁忙，请稍后重试");
    }

    private AppChainDepositOrderData toApp(BizChainDeposit row)
    {
        String network = ChainNetwork.normalizeOrDefault(row.getNetwork());
        boolean bep20 = ChainNetwork.isBep20(network);
        AppChainDepositOrderData data = new AppChainDepositOrderData();
        data.setOutTradeNo(row.getOutTradeNo());
        data.setRechargeId(row.getRechargeId());
        data.setNetwork(network);
        data.setAddress(row.getAddress());
        data.setAddressSource(row.getAddressSource());
        data.setAsset(row.getAsset());
        data.setCurrency(row.getCurrency());
        data.setContractAddress(bep20 ? BizConstants.USDT_BEP20_CONTRACT : BizConstants.USDT_TRC20_CONTRACT);
        data.setDecimals(Integer.valueOf(bep20 ? 18 : 6));
        data.setAmount(row.getAmount());
        data.setPayAmount(row.getPayAmount());
        data.setPayAmountText(formatPay(row.getPayAmount()));
        data.setExpireTime(formatTime(row.getExpireTime()));
        data.setExpireTimeUtc(formatUtc(row.getExpireTime()));
        data.setExpireAt(expireAt(row.getExpireTime()));
        data.setRemainSeconds(remainSeconds(row.getExpireTime()));
        data.setStatus(row.getStatus());
        data.setTxHash(row.getTxHash());
        data.setHint(hint());
        return data;
    }

    private static AppChainDepositNetworkItem networkItem(String network, String name, String asset,
            ChainCollectTarget target, String contract, int decimals, boolean enabled)
    {
        AppChainDepositNetworkItem item = new AppChainDepositNetworkItem();
        item.setNetwork(network);
        item.setName(name);
        item.setAsset(asset);
        item.setAddress(enabled && target != null ? target.getAddress() : "");
        item.setAddressSource(enabled && target != null ? target.getSource() : "");
        item.setContractAddress(contract);
        item.setDecimals(Integer.valueOf(decimals));
        item.setEnabled(Boolean.valueOf(enabled));
        return item;
    }

    private IBizChainDepositService proxy()
    {
        return applicationContext.getBean(IBizChainDepositService.class);
    }

    private boolean bool(String key, boolean defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private String config(String key, String defaultValue)
    {
        String value = sysConfigService.selectConfigByKey(key);
        return StringUtils.isEmpty(value) ? defaultValue : value.trim();
    }

    private int expireMinutes()
    {
        try
        {
            int n = Integer.parseInt(config(BizConstants.CONFIG_CHAIN_EXPIRE_MINUTES, "30"));
            return n < 5 ? 5 : n;
        }
        catch (Exception e)
        {
            return 30;
        }
    }

    private BigDecimal minAmount()
    {
        try
        {
            return new BigDecimal(config(BizConstants.CONFIG_CHAIN_MIN_AMOUNT, "10"));
        }
        catch (Exception e)
        {
            return new BigDecimal("10");
        }
    }

    private BigDecimal maxAmount()
    {
        try
        {
            BigDecimal max = new BigDecimal(config(BizConstants.CONFIG_CHAIN_MAX_AMOUNT, "100000"));
            if (max.compareTo(BigDecimal.ZERO) <= 0)
            {
                return null;
            }
            return max;
        }
        catch (Exception e)
        {
            return new BigDecimal("100000");
        }
    }

    private String hint()
    {
        String value = config(BizConstants.CONFIG_CHAIN_HINT, DEFAULT_HINT);
        return StringUtils.isEmpty(value) ? DEFAULT_HINT : value;
    }

    private static String nextOutTradeNo(Long memberId)
    {
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        long tail = memberId == null ? 0L : memberId.longValue() % 10000L;
        return "C" + time + String.format("%04d", Long.valueOf(tail));
    }

    private static Date minutesLater(int minutes)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    private static String formatTime(Date date)
    {
        if (date == null)
        {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private static String formatUtc(Date date)
    {
        if (date == null)
        {
            return "";
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        fmt.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return fmt.format(date);
    }

    private static Long expireAt(Date date)
    {
        return date == null ? null : Long.valueOf(date.getTime());
    }

    private static Integer remainSeconds(Date date)
    {
        if (date == null)
        {
            return Integer.valueOf(0);
        }
        long sec = (date.getTime() - System.currentTimeMillis()) / 1000L;
        if (sec < 0L)
        {
            sec = 0L;
        }
        if (sec > Integer.MAX_VALUE)
        {
            sec = Integer.MAX_VALUE;
        }
        return Integer.valueOf((int) sec);
    }

    private static String formatPay(BigDecimal pay)
    {
        if (pay == null)
        {
            return "0.000000";
        }
        return pay.setScale(6, RoundingMode.DOWN).toPlainString();
    }

    private static String cut(String raw, int max)
    {
        if (raw == null)
        {
            return "";
        }
        return raw.length() <= max ? raw : raw.substring(0, max);
    }

    private static String mask(String address)
    {
        if (address == null || address.length() < 8)
        {
            return "";
        }
        return address.substring(0, 4) + "****" + address.substring(address.length() - 4);
    }
}
