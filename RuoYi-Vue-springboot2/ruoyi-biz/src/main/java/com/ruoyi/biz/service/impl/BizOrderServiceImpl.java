package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.domain.BizOrderUnlockLot;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.domain.BizRebateLog;
import com.ruoyi.biz.mapper.BizOrderMapper;
import com.ruoyi.biz.mapper.BizOrderUnlockLotMapper;
import com.ruoyi.biz.mapper.BizProductMapper;
import com.ruoyi.biz.mapper.BizRebateLogMapper;
import com.ruoyi.biz.service.IBizCommissionService;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.biz.service.IBizOrderService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

@Service
public class BizOrderServiceImpl implements IBizOrderService
{
    private static final Logger log = LoggerFactory.getLogger(BizOrderServiceImpl.class);

    @Autowired
    private BizOrderMapper orderMapper;

    @Autowired
    private BizProductMapper productMapper;

    @Autowired
    private BizOrderUnlockLotMapper lotMapper;

    @Autowired
    private BizRebateLogMapper rebateLogMapper;

    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizMemberService memberService;

    @Autowired
    private IBizConfigService configService;

    @Autowired
    private IBizCommissionService commissionService;

    @Override
    public BizOrder selectOrderById(Long orderId)
    {
        return fillActivate(orderMapper.selectOrderById(orderId), new UnlockSupport());
    }

    @Override
    public List<BizOrder> selectOrderList(BizOrder order)
    {
        List<BizOrder> list = orderMapper.selectOrderList(order);
        if (list != null)
        {
            UnlockSupport support = new UnlockSupport();
            for (int i = 0; i < list.size(); i++)
            {
                fillActivate(list.get(i), support);
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizOrder subscribe(Long memberId, Long productId, String payCurrency, String payPassword, Integer quantity)
    {
        memberService.assertPayPassword(memberId, payPassword);
        BizMember member = memberService.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        if (BizConstants.STATUS_DISABLE.equals(member.getStatus()))
        {
            throw new ServiceException("账号已停用");
        }
        BizProduct product = productMapper.selectProductById(productId);
        if (product == null || BizConstants.STATUS_DISABLE.equals(product.getStatus()))
        {
            throw new ServiceException("产品不存在或已下架");
        }
        if (!product.saleOpen())
        {
            throw new ServiceException("产品暂未开售");
        }
        int qty = resolveQuantity(quantity);
        Integer buyLimit = product.getBuyLimit();
        if (buyLimit != null && buyLimit.intValue() > 0)
        {
            int bought = orderMapper.countMemberProductOrders(memberId, productId);
            int remain = buyLimit.intValue() - bought;
            if (remain <= 0)
            {
                throw new ServiceException("该产品每人限购" + buyLimit + "份");
            }
            if (qty > remain)
            {
                throw new ServiceException("该产品每人限购" + buyLimit + "份，还可认购" + remain + "份");
            }
        }
        String currency = resolvePayCurrency(product, payCurrency);
        configService.assertCurrencyEnabled(currency);
        BigDecimal unitPrice = product.priceOf(currency);
        if (!BizProduct.hasPrice(unitPrice))
        {
            throw new ServiceException("USDT".equals(currency) ? "该产品不支持USDT认购" : "该产品不支持人民币认购");
        }
        BigDecimal unitRebate = product.rebateOf(currency);
        if (unitRebate == null)
        {
            unitRebate = BigDecimal.ZERO;
        }
        BigDecimal qtyDec = new BigDecimal(qty);
        BigDecimal price = unitPrice.multiply(qtyDec);
        BigDecimal rebate = unitRebate.multiply(qtyDec);
        String remark = "认购产品:" + product.getProductName();
        if (qty > 1)
        {
            remark = remark + " x" + qty;
        }

        walletService.debit(memberId, currency, price, BizConstants.BIZ_SUBSCRIBE,
                productId, remark);

        BizOrder order = new BizOrder();
        order.setOrderNo(DateUtils.dateTimeNow() + IdUtils.fastSimpleUUID().substring(0, 8));
        order.setMemberId(memberId);
        order.setProductId(product.getProductId());
        order.setProductName(product.getProductName());
        order.setCurrency(currency);
        order.setPrice(price);
        order.setQuantity(Integer.valueOf(qty));
        order.setDailyRebate(rebate);
        order.setDurationDays(product.getDurationDays());
        order.setRemainingDays(product.getDurationDays());
        order.setWithdrawRequired(product.getWithdrawRequired());
        order.setUnlockDirectQty(nz(product.getUnlockDirectQty()));
        order.setUnlockDelayHours(nz(product.getUnlockDelayHours()));
        order.setStatus(BizConstants.ORDER_HOLDING);
        orderMapper.insertOrder(order);
        commissionService.grantForSubscribe(order);

        memberService.refreshLevelAndUplines(memberId);
        UnlockSupport support = new UnlockSupport();
        refreshUnlock(memberId, productId, support);
        if (member.getParentId() != null)
        {
            refreshUnlock(member.getParentId(), productId, support);
        }
        return fillActivate(orderMapper.selectOrderById(order.getOrderId()), support);
    }

    private int resolveQuantity(Integer quantity)
    {
        if (quantity == null)
        {
            return 1;
        }
        if (quantity.intValue() < 1)
        {
            throw new ServiceException("认购数量必须大于0");
        }
        return quantity.intValue();
    }

    private String resolvePayCurrency(BizProduct product, String payCurrency)
    {
        if (StringUtils.isEmpty(payCurrency))
        {
            if (BizProduct.hasPrice(product.getPriceCny()))
            {
                return BizConstants.CURRENCY_CNY;
            }
            if (BizProduct.hasPrice(product.getPriceUsdt()))
            {
                return BizConstants.CURRENCY_USDT;
            }
            throw new ServiceException("产品未配置认购价格");
        }
        String currency = payCurrency.toUpperCase();
        if (!BizConstants.CURRENCY_CNY.equals(currency) && !BizConstants.CURRENCY_USDT.equals(currency))
        {
            throw new ServiceException("请选择人民币或USDT认购");
        }
        return currency;
    }

    @Override
    public int processDailyRebate()
    {
        Date today = DateUtils.parseDate(DateUtils.getDate());
        List<BizOrder> orders = orderMapper.selectHoldingOrders();
        int success = 0;
        UnlockSupport support = new UnlockSupport();
        for (BizOrder order : orders)
        {
            try
            {
                SpringUtils.getAopProxy(this).rebateOne(order, today, support);
                success++;
            }
            catch (Exception e)
            {
                log.error("订单{}每日返利失败: {}", order.getOrderId(), e.getMessage());
            }
        }
        return success;
    }

    @Transactional(rollbackFor = Exception.class)
    public void rebateOne(BizOrder order, Date today)
    {
        rebateOne(order, today, new UnlockSupport());
    }

    @Transactional(rollbackFor = Exception.class)
    public void rebateOne(BizOrder order, Date today, UnlockSupport support)
    {
        if (order == null)
        {
            return;
        }
        fillActivate(order, support);
        List<BizOrderUnlockLot> lots = support.lotsOf(order.getOrderId());
        if (lots == null || lots.isEmpty())
        {
            return;
        }
        BigDecimal unit = unitRebate(order);
        BigDecimal paid = BigDecimal.ZERO;
        Date now = DateUtils.getNowDate();
        for (int i = 0; i < lots.size(); i++)
        {
            BizOrderUnlockLot lot = lots.get(i);
            int remain = nz(lot.getRemainingDays());
            if (lot.getIncomeStartTime() == null || now.before(lot.getIncomeStartTime()))
            {
                continue;
            }
            if (remain <= 0)
            {
                continue;
            }
            if (lot.getLastRebateDate() != null && !today.after(lot.getLastRebateDate()))
            {
                continue;
            }
            paid = paid.add(unit.multiply(new BigDecimal(qtyOfLot(lot))));
            lot.setRemainingDays(Integer.valueOf(remain - 1));
            lot.setLastRebateDate(today);
            lotMapper.updateLot(lot);
        }
        int remainMax = 0;
        boolean allLotsDone = lots.size() > 0;
        for (int i = 0; i < lots.size(); i++)
        {
            int remain = nz(lots.get(i).getRemainingDays());
            if (remain > remainMax)
            {
                remainMax = remain;
            }
            if (remain > 0)
            {
                allLotsDone = false;
            }
        }
        boolean allActivated = nz(order.getActivatedQty()) >= qtyOf(order);
        if (paid.compareTo(BigDecimal.ZERO) > 0)
        {
            String currency = StringUtils.isEmpty(order.getCurrency())
                    ? BizConstants.CURRENCY_CNY : order.getCurrency().toUpperCase();
            walletService.credit(order.getMemberId(), currency, paid,
                    BizConstants.BIZ_REBATE, order.getOrderId(), "产品每日返利");
            BizRebateLog rebateLog = new BizRebateLog();
            rebateLog.setOrderId(order.getOrderId());
            rebateLog.setMemberId(order.getMemberId());
            rebateLog.setCurrency(currency);
            rebateLog.setAmount(paid);
            rebateLog.setRebateDate(today);
            rebateLogMapper.insertRebateLog(rebateLog);
        }
        BizOrder update = new BizOrder();
        update.setOrderId(order.getOrderId());
        if (paid.compareTo(BigDecimal.ZERO) > 0)
        {
            update.setLastRebateDate(today);
        }
        if (!allActivated)
        {
            update.setRemainingDays(Integer.valueOf(Math.max(remainMax, 1)));
        }
        else
        {
            update.setRemainingDays(Integer.valueOf(remainMax));
            if (allLotsDone)
            {
                update.setStatus(BizConstants.ORDER_FINISHED);
            }
        }
        orderMapper.updateOrder(update);
    }

    private void refreshUnlock(Long memberId, Long productId, UnlockSupport support)
    {
        if (memberId == null || productId == null)
        {
            return;
        }
        support.plan(memberId, productId);
    }

    private BizOrder fillActivate(BizOrder order, UnlockSupport support)
    {
        if (order == null)
        {
            return null;
        }
        UnlockPlan plan = support.plan(order.getMemberId(), order.getProductId());
        List<BizOrderUnlockLot> lots = plan == null ? null : plan.lotsByOrder.get(order.getOrderId());
        if (lots == null)
        {
            lots = lotMapper.selectByOrderId(order.getOrderId());
        }
        if (lots == null)
        {
            lots = new ArrayList<BizOrderUnlockLot>();
        }
        support.rememberLots(order.getOrderId(), lots);
        Date now = DateUtils.getNowDate();
        int activated = lots.size();
        int ready = 0;
        Date nextStart = null;
        Date firstStart = null;
        for (int i = 0; i < lots.size(); i++)
        {
            BizOrderUnlockLot lot = lots.get(i);
            Date start = lot.getIncomeStartTime();
            if (start != null && (firstStart == null || start.before(firstStart)))
            {
                firstStart = start;
            }
            if (start != null && !now.before(start))
            {
                ready++;
            }
            else if (start != null && (nextStart == null || start.before(nextStart)))
            {
                nextStart = start;
            }
        }
        int down = plan == null ? 0 : plan.downQty;
        order.setUnlockDirectHave(Integer.valueOf(down));
        order.setActivatedQty(Integer.valueOf(activated));
        order.setIncomeReadyQty(Integer.valueOf(ready));
        order.setActivateStatus(activated > 0 ? "1" : "0");
        order.setIncomeReady(Boolean.valueOf(ready > 0));
        if (activated <= 0)
        {
            order.setIncomeStartTime(null);
        }
        else if (nextStart != null)
        {
            order.setIncomeStartTime(nextStart);
        }
        else
        {
            order.setIncomeStartTime(firstStart);
        }
        order.setIncomeDailyRebate(unitRebate(order).multiply(new BigDecimal(ready)));
        return order;
    }

    private BigDecimal unitRebate(BizOrder order)
    {
        int qty = qtyOf(order);
        BigDecimal total = order.getDailyRebate() == null ? BigDecimal.ZERO : order.getDailyRebate();
        if (qty <= 1)
        {
            return total;
        }
        return total.divide(new BigDecimal(qty), 8, RoundingMode.HALF_UP);
    }

    private int qtyOf(BizOrder order)
    {
        int qty = nz(order.getQuantity());
        return qty <= 0 ? 1 : qty;
    }

    private int qtyOfLot(BizOrderUnlockLot lot)
    {
        int qty = nz(lot.getQty());
        return qty <= 0 ? 1 : qty;
    }

    private Date plusHours(Date time, int hours)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    private Date firstReachTime(List<BizOrder> downs, int need)
    {
        if (downs == null || need <= 0)
        {
            return null;
        }
        int acc = 0;
        for (int i = 0; i < downs.size(); i++)
        {
            acc += qtyOf(downs.get(i));
            if (acc >= need)
            {
                Date time = downs.get(i).getCreateTime();
                return time != null ? time : DateUtils.getNowDate();
            }
        }
        return null;
    }

    private boolean sameTier(Long productIdA, Long productIdB, UnlockSupport support)
    {
        if (productIdA != null && productIdA.equals(productIdB))
        {
            return true;
        }
        BizProduct a = support.product(productIdA);
        BizProduct b = support.product(productIdB);
        if (a == null || b == null)
        {
            return false;
        }
        boolean aCny = BizProduct.hasPrice(a.getPriceCny());
        boolean bCny = BizProduct.hasPrice(b.getPriceCny());
        boolean aUsdt = BizProduct.hasPrice(a.getPriceUsdt());
        boolean bUsdt = BizProduct.hasPrice(b.getPriceUsdt());
        if (aCny && bCny && a.getPriceCny().compareTo(b.getPriceCny()) == 0)
        {
            return true;
        }
        if (aUsdt && bUsdt && a.getPriceUsdt().compareTo(b.getPriceUsdt()) == 0)
        {
            return true;
        }
        BigDecimal fx = support.fx();
        if (aCny && bUsdt && eqMoney(a.getPriceCny(), b.getPriceUsdt().multiply(fx)))
        {
            return true;
        }
        if (aUsdt && bCny && eqMoney(b.getPriceCny(), a.getPriceUsdt().multiply(fx)))
        {
            return true;
        }
        return false;
    }

    private boolean eqMoney(BigDecimal left, BigDecimal right)
    {
        if (left == null || right == null)
        {
            return false;
        }
        return left.setScale(2, RoundingMode.HALF_UP).compareTo(right.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    private int nz(Integer value)
    {
        return value == null ? 0 : value.intValue();
    }

    private class UnlockPlan
    {
        private int downQty;
        private final Map<Long, List<BizOrderUnlockLot>> lotsByOrder = new HashMap<Long, List<BizOrderUnlockLot>>();
    }

    private class UnlockSupport
    {
        private final Map<Long, BizProduct> products = new HashMap<Long, BizProduct>();
        private final Map<Long, List<BizOrder>> holdingByMember = new HashMap<Long, List<BizOrder>>();
        private final Map<Long, List<BizOrder>> downByParent = new HashMap<Long, List<BizOrder>>();
        private final Map<String, UnlockPlan> plans = new HashMap<String, UnlockPlan>();
        private final Map<Long, List<BizOrderUnlockLot>> lots = new HashMap<Long, List<BizOrderUnlockLot>>();
        private BigDecimal fxRate;

        private BigDecimal fx()
        {
            if (fxRate == null)
            {
                fxRate = configService.getUsdtToCnyRate();
            }
            return fxRate;
        }

        private BizProduct product(Long productId)
        {
            if (productId == null)
            {
                return null;
            }
            if (!products.containsKey(productId))
            {
                products.put(productId, productMapper.selectProductById(productId));
            }
            return products.get(productId);
        }

        private List<BizOrder> holding(Long memberId)
        {
            if (memberId == null)
            {
                return new ArrayList<BizOrder>();
            }
            if (!holdingByMember.containsKey(memberId))
            {
                BizOrder query = new BizOrder();
                query.setMemberId(memberId);
                query.setStatus(BizConstants.ORDER_HOLDING);
                List<BizOrder> list = orderMapper.selectOrderList(query);
                if (list == null)
                {
                    list = new ArrayList<BizOrder>();
                }
                list.sort(new Comparator<BizOrder>()
                {
                    @Override
                    public int compare(BizOrder a, BizOrder b)
                    {
                        Date ta = a.getCreateTime();
                        Date tb = b.getCreateTime();
                        if (ta == null && tb == null)
                        {
                            return Long.compare(nzId(a), nzId(b));
                        }
                        if (ta == null)
                        {
                            return 1;
                        }
                        if (tb == null)
                        {
                            return -1;
                        }
                        int c = ta.compareTo(tb);
                        return c != 0 ? c : Long.compare(nzId(a), nzId(b));
                    }
                });
                holdingByMember.put(memberId, list);
            }
            return holdingByMember.get(memberId);
        }

        private List<BizOrder> downs(Long parentId)
        {
            if (parentId == null)
            {
                return new ArrayList<BizOrder>();
            }
            if (!downByParent.containsKey(parentId))
            {
                List<BizOrder> list = orderMapper.selectDirectDownlineOrders(parentId);
                downByParent.put(parentId, list == null ? new ArrayList<BizOrder>() : list);
            }
            return downByParent.get(parentId);
        }

        private void rememberLots(Long orderId, List<BizOrderUnlockLot> list)
        {
            lots.put(orderId, list);
        }

        private List<BizOrderUnlockLot> lotsOf(Long orderId)
        {
            return lots.get(orderId);
        }

        private UnlockPlan plan(Long memberId, Long productId)
        {
            if (memberId == null || productId == null)
            {
                return null;
            }
            String key = memberId + ":" + productId;
            UnlockPlan cached = plans.get(key);
            if (cached != null)
            {
                return cached;
            }
            List<BizOrder> parents = new ArrayList<BizOrder>();
            List<BizOrder> holding = holding(memberId);
            for (int i = 0; i < holding.size(); i++)
            {
                BizOrder row = holding.get(i);
                if (sameTier(productId, row.getProductId(), this))
                {
                    parents.add(row);
                }
            }
            List<BizOrder> tierDowns = new ArrayList<BizOrder>();
            List<BizOrder> allDowns = downs(memberId);
            for (int i = 0; i < allDowns.size(); i++)
            {
                BizOrder row = allDowns.get(i);
                if (sameTier(productId, row.getProductId(), this))
                {
                    tierDowns.add(row);
                }
            }
            int downQty = 0;
            for (int i = 0; i < tierDowns.size(); i++)
            {
                downQty += qtyOf(tierDowns.get(i));
            }
            int need = 0;
            for (int i = 0; i < parents.size(); i++)
            {
                int n = nz(parents.get(i).getUnlockDirectQty());
                if (n > 0)
                {
                    need = n;
                    break;
                }
            }
            int quota = need <= 0 ? Integer.MAX_VALUE : downQty / need;
            UnlockPlan plan = new UnlockPlan();
            plan.downQty = downQty;
            int cursor = 0;
            for (int i = 0; i < parents.size(); i++)
            {
                BizOrder parent = parents.get(i);
                int oQty = qtyOf(parent);
                int oNeed = nz(parent.getUnlockDirectQty());
                int activate;
                int startIndex;
                if (oNeed <= 0)
                {
                    activate = oQty;
                    startIndex = -1;
                }
                else
                {
                    activate = Math.max(0, Math.min(cursor + oQty, quota) - cursor);
                    startIndex = cursor;
                    cursor += oQty;
                }
                List<BizOrderUnlockLot> orderLots = syncLots(parent, activate, startIndex, need, oNeed, tierDowns);
                plan.lotsByOrder.put(parent.getOrderId(), orderLots);
                rememberLots(parent.getOrderId(), orderLots);
                String alias = memberId + ":" + parent.getProductId();
                plans.put(alias, plan);
            }
            plans.put(key, plan);
            return plan;
        }

        private List<BizOrderUnlockLot> syncLots(BizOrder order, int activate, int startIndex, int need,
                int orderNeed, List<BizOrder> tierDowns)
        {
            List<BizOrderUnlockLot> existing = lotMapper.selectByOrderId(order.getOrderId());
            if (existing == null)
            {
                existing = new ArrayList<BizOrderUnlockLot>();
            }
            Set<Integer> have = new HashSet<Integer>();
            for (int i = 0; i < existing.size(); i++)
            {
                have.add(Integer.valueOf(nz(existing.get(i).getShareNo())));
            }
            boolean inherit = existing.isEmpty() && order.getLastRebateDate() != null;
            Date ownTime = order.getCreateTime() != null ? order.getCreateTime() : DateUtils.getNowDate();
            int delay = nz(order.getUnlockDelayHours());
            int duration = order.getDurationDays() == null ? 0 : order.getDurationDays().intValue();
            int inheritRemain = nz(order.getRemainingDays());
            if (inheritRemain <= 0)
            {
                inheritRemain = duration;
            }
            for (int shareNo = 0; shareNo < activate; shareNo++)
            {
                if (have.contains(Integer.valueOf(shareNo)))
                {
                    continue;
                }
                Date activateTime = ownTime;
                if (orderNeed > 0 && startIndex >= 0)
                {
                    Date reached = firstReachTime(tierDowns, (startIndex + shareNo + 1) * need);
                    if (reached == null)
                    {
                        continue;
                    }
                    if (reached.after(ownTime))
                    {
                        activateTime = reached;
                    }
                }
                BizOrderUnlockLot lot = new BizOrderUnlockLot();
                lot.setOrderId(order.getOrderId());
                lot.setShareNo(Integer.valueOf(shareNo));
                lot.setQty(Integer.valueOf(1));
                lot.setActivateTime(activateTime);
                lot.setIncomeStartTime(plusHours(activateTime, delay));
                if (inherit)
                {
                    lot.setRemainingDays(Integer.valueOf(inheritRemain));
                    lot.setLastRebateDate(order.getLastRebateDate());
                }
                else
                {
                    lot.setRemainingDays(Integer.valueOf(duration));
                }
                lotMapper.insertLot(lot);
                existing.add(lot);
            }
            if (activate > 0 && order.getIncomeStartTime() == null && !existing.isEmpty())
            {
                Date first = existing.get(0).getIncomeStartTime();
                for (int i = 1; i < existing.size(); i++)
                {
                    Date start = existing.get(i).getIncomeStartTime();
                    if (start != null && (first == null || start.before(first)))
                    {
                        first = start;
                    }
                }
                if (first != null)
                {
                    BizOrder update = new BizOrder();
                    update.setOrderId(order.getOrderId());
                    update.setIncomeStartTime(first);
                    orderMapper.updateOrder(update);
                    order.setIncomeStartTime(first);
                }
            }
            existing.sort(new Comparator<BizOrderUnlockLot>()
            {
                @Override
                public int compare(BizOrderUnlockLot a, BizOrderUnlockLot b)
                {
                    return Integer.compare(nz(a.getShareNo()), nz(b.getShareNo()));
                }
            });
            return existing;
        }

        private long nzId(BizOrder order)
        {
            return order.getOrderId() == null ? 0L : order.getOrderId().longValue();
        }
    }
}
