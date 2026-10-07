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
        BizOrder order = fillActivate(orderMapper.selectOrderById(orderId), new UnlockSupport());
        fillAccumulateFlags(order);
        return order;
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
                fillAccumulateFlags(list.get(i));
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
        BigDecimal qtyDec = new BigDecimal(qty);
        BigDecimal price = unitPrice.multiply(qtyDec);
        boolean assistMode = product.assistMode();
        BigDecimal rebate = BigDecimal.ZERO;
        if (!assistMode)
        {
            BigDecimal unitRebate = product.rebateOf(currency);
            if (unitRebate == null)
            {
                unitRebate = BigDecimal.ZERO;
            }
            rebate = unitRebate.multiply(qtyDec);
        }
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
        order.setWithdrawRequired(product.getWithdrawRequired());
        order.setStatus(BizConstants.ORDER_HOLDING);
        order.setCreateTime(DateUtils.getNowDate());
        order.setRelatedSlotsUsed(Integer.valueOf(0));
        order.setAccumulateSettledShares(Integer.valueOf(0));

        if (assistMode)
        {
            int returnDays = product.getPrincipalReturnDays() == null ? 0 : product.getPrincipalReturnDays().intValue();
            if (returnDays <= 0)
            {
                throw new ServiceException("助力产品请配置本金返还天数");
            }
            java.util.List<BizProduct.AssistGrant> grants = product.resolveAssistGrants(currency);
            if (grants == null || grants.isEmpty())
            {
                throw new ServiceException("助力产品请按发放模式配置助力值");
            }
            BigDecimal assistSnapshot = BigDecimal.ZERO;
            for (BizProduct.AssistGrant g : grants)
            {
                if (g != null && g.valid() && "CNY".equalsIgnoreCase(g.getCurrency()))
                {
                    assistSnapshot = g.getUnit().multiply(qtyDec);
                    break;
                }
            }
            if (assistSnapshot.compareTo(BigDecimal.ZERO) <= 0)
            {
                assistSnapshot = grants.get(0).getUnit().multiply(qtyDec);
            }
            order.setBizMode(BizConstants.BIZ_MODE_ASSIST);
            order.setAssistValue(assistSnapshot);
            order.setPrincipalReturnDays(Integer.valueOf(returnDays));
            order.setPrincipalReturnAt(plusDays(DateUtils.getNowDate(), returnDays));
            order.setPrincipalReturned("0");
            order.setDurationDays(Integer.valueOf(0));
            order.setRemainingDays(Integer.valueOf(0));
            order.setUnlockDirectQty(Integer.valueOf(0));
            order.setUnlockDelayHours(Integer.valueOf(0));
            order.setIncomeMode(BizConstants.INCOME_MODE_CREDIT);
            order.setAccumulateCycleDays(Integer.valueOf(0));
            order.setProtectDays(Integer.valueOf(0));
            order.setRelatedProductId(null);
            order.setAccumulatedAmount(BigDecimal.ZERO);
            order.setAccumulateDays(Integer.valueOf(0));
            order.setAccumulatePaused("0");
            orderMapper.insertOrder(order);

            creditAssistGrants(memberId, product, order.getOrderId(), grants, qtyDec);
            commissionService.grantForSubscribe(order);

            memberService.refreshLevelAndUplines(memberId);
            return fillActivate(orderMapper.selectOrderById(order.getOrderId()), new UnlockSupport());
        }

        order.setBizMode(BizConstants.BIZ_MODE_REBATE);
        java.util.List<BizProduct.AssistGrant> rebateGrants = product.resolveAssistGrants(currency);
        BigDecimal rebateAssistSnapshot = BigDecimal.ZERO;
        if (rebateGrants != null && !rebateGrants.isEmpty())
        {
            for (BizProduct.AssistGrant g : rebateGrants)
            {
                if (g != null && g.valid() && "CNY".equalsIgnoreCase(g.getCurrency()))
                {
                    rebateAssistSnapshot = g.getUnit().multiply(qtyDec);
                    break;
                }
            }
            if (rebateAssistSnapshot.compareTo(BigDecimal.ZERO) <= 0)
            {
                rebateAssistSnapshot = rebateGrants.get(0).getUnit().multiply(qtyDec);
            }
        }
        order.setAssistValue(rebateAssistSnapshot);
        order.setPrincipalReturnDays(Integer.valueOf(0));
        order.setPrincipalReturned("0");
        order.setDurationDays(product.getDurationDays());
        order.setRemainingDays(product.getDurationDays());
        order.setUnlockDirectQty(nz(product.getUnlockDirectQty()));
        order.setUnlockDelayHours(nz(product.getUnlockDelayHours()));
        if (product.protectIncome())
        {
            order.setIncomeMode(BizConstants.INCOME_MODE_PROTECT);
            order.setProtectDays(Integer.valueOf(nz(product.getProtectDays())));
            order.setAccumulateCycleDays(Integer.valueOf(0));
            order.setRelatedProductId(null);
        }
        else if (product.accumulateIncome())
        {
            order.setIncomeMode(BizConstants.INCOME_MODE_ACCUMULATE);
            order.setAccumulateCycleDays(nz(product.getAccumulateCycleDays()));
            order.setProtectDays(Integer.valueOf(0));
            order.setRelatedProductId(product.getRelatedProductId());
        }
        else
        {
            order.setIncomeMode(BizConstants.INCOME_MODE_CREDIT);
            order.setAccumulateCycleDays(Integer.valueOf(0));
            order.setProtectDays(Integer.valueOf(0));
            order.setRelatedProductId(null);
        }
        order.setAccumulatedAmount(BigDecimal.ZERO);
        order.setAccumulateDays(Integer.valueOf(0));
        order.setAccumulatePaused("0");
        orderMapper.insertOrder(order);
        creditAssistGrants(memberId, product, order.getOrderId(), rebateGrants, qtyDec);
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

    private void creditAssistGrants(Long memberId, BizProduct product, Long orderId,
            java.util.List<BizProduct.AssistGrant> grants, BigDecimal qtyDec)
    {
        if (grants == null || grants.isEmpty() || product == null || orderId == null)
        {
            return;
        }
        for (BizProduct.AssistGrant g : grants)
        {
            if (g == null || !g.valid())
            {
                continue;
            }
            BigDecimal total = g.getUnit().multiply(qtyDec);
            walletService.credit(memberId, g.getCurrency(), total, BizConstants.BIZ_ASSIST_GRANT,
                    orderId, "认购发放助力值:" + product.getProductName(), BizConstants.WALLET_ASSIST);
        }
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

    @Override
    public int processAssistPrincipalReturn()
    {
        List<BizOrder> orders = orderMapper.selectAssistDueReturnOrders();
        int success = 0;
        for (int i = 0; i < orders.size(); i++)
        {
            BizOrder order = orders.get(i);
            try
            {
                SpringUtils.getAopProxy(this).returnAssistPrincipal(order);
                success++;
            }
            catch (Exception e)
            {
                log.error("订单{}助力退本失败: {}", order.getOrderId(), e.getMessage());
            }
        }
        return success;
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnAssistPrincipal(BizOrder order)
    {
        if (order == null || !order.assistMode())
        {
            return;
        }
        if ("1".equals(order.getPrincipalReturned()))
        {
            return;
        }
        if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("退本金额无效");
        }
        walletService.credit(order.getMemberId(), order.getCurrency(), order.getPrice(),
                BizConstants.BIZ_PRINCIPAL_RETURN, order.getOrderId(),
                "星航助力退本:" + order.getProductName(), BizConstants.WALLET_BALANCE);
        BizOrder patch = new BizOrder();
        patch.setOrderId(order.getOrderId());
        patch.setPrincipalReturned("1");
        patch.setStatus(BizConstants.ORDER_FINISHED);
        orderMapper.updateOrder(patch);
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
        boolean accumulate = order.accumulateIncome();
        if (accumulate && "1".equals(order.getAccumulatePaused()))
        {
            return;
        }
        fillActivate(order, support);
        List<BizOrderUnlockLot> lots = support.lotsOf(order.getOrderId());
        if (lots == null || lots.isEmpty())
        {
            return;
        }
        if (order.protectIncome())
        {
            rebateProtect(order, today, lots);
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
        BizOrder update = new BizOrder();
        update.setOrderId(order.getOrderId());
        if (paid.compareTo(BigDecimal.ZERO) > 0)
        {
            String currency = StringUtils.isEmpty(order.getCurrency())
                    ? BizConstants.CURRENCY_CNY : order.getCurrency().toUpperCase();
            BizRebateLog rebateLog = new BizRebateLog();
            rebateLog.setOrderId(order.getOrderId());
            rebateLog.setMemberId(order.getMemberId());
            rebateLog.setCurrency(currency);
            rebateLog.setAmount(paid);
            rebateLog.setRebateDate(today);
            rebateLogMapper.insertRebateLog(rebateLog);
            update.setLastRebateDate(today);

            if (accumulate)
            {
                BigDecimal prev = order.getAccumulatedAmount() == null ? BigDecimal.ZERO : order.getAccumulatedAmount();
                int days = nz(order.getAccumulateDays()) + 1;
                update.setAccumulatedAmount(prev.add(paid));
                update.setAccumulateDays(Integer.valueOf(days));
                update.setLastAccumulateDate(today);
                if (order.getAccumulateCycleStartAt() == null)
                {
                    update.setAccumulateCycleStartAt(now);
                }
                int cycle = nz(order.getAccumulateCycleDays());
                if (cycle > 0 && days >= cycle)
                {
                    update.setAccumulatePaused("1");
                }
            }
            else
            {
                walletService.credit(order.getMemberId(), currency, paid,
                        BizConstants.BIZ_REBATE, order.getOrderId(), "产品每日返利",
                        BizConstants.WALLET_PRODUCT);
            }
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

    private void rebateProtect(BizOrder order, Date today, List<BizOrderUnlockLot> lots)
    {
        BigDecimal unit = unitRebate(order);
        BigDecimal paidWallet = BigDecimal.ZERO;
        BigDecimal paidPool = BigDecimal.ZERO;
        Date now = DateUtils.getNowDate();
        int protectDays = nz(order.getProtectDays());
        int duration = order.getDurationDays() == null ? 0 : order.getDurationDays().intValue();
        for (int i = 0; i < lots.size(); i++)
        {
            BizOrderUnlockLot lot = lots.get(i);
            int remain = nz(lot.getRemainingDays());
            if (remain <= 0)
            {
                continue;
            }
            if (lot.getLastRebateDate() != null && !today.after(lot.getLastRebateDate()))
            {
                continue;
            }
            BigDecimal piece = unit.multiply(new BigDecimal(qtyOfLot(lot)));
            int paidCount = duration > 0 ? Math.max(0, duration - remain) : 0;
            boolean inProtect = protectDays <= 0 || paidCount < protectDays;
            if (inProtect || lot.getActivateTime() != null)
            {
                paidWallet = paidWallet.add(piece);
            }
            else
            {
                paidPool = paidPool.add(piece);
            }
            lot.setRemainingDays(Integer.valueOf(remain - 1));
            lot.setLastRebateDate(today);
            lotMapper.updateLot(lot);
        }
        BigDecimal paid = paidWallet.add(paidPool);
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
        BizOrder update = new BizOrder();
        update.setOrderId(order.getOrderId());
        if (paid.compareTo(BigDecimal.ZERO) > 0)
        {
            String currency = StringUtils.isEmpty(order.getCurrency())
                    ? BizConstants.CURRENCY_CNY : order.getCurrency().toUpperCase();
            BizRebateLog rebateLog = new BizRebateLog();
            rebateLog.setOrderId(order.getOrderId());
            rebateLog.setMemberId(order.getMemberId());
            rebateLog.setCurrency(currency);
            rebateLog.setAmount(paid);
            rebateLog.setRebateDate(today);
            rebateLogMapper.insertRebateLog(rebateLog);
            update.setLastRebateDate(today);
            if (paidWallet.compareTo(BigDecimal.ZERO) > 0)
            {
                walletService.credit(order.getMemberId(), currency, paidWallet,
                        BizConstants.BIZ_REBATE, order.getOrderId(), "产品每日返利",
                        BizConstants.WALLET_PRODUCT);
            }
            if (paidPool.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal prev = order.getAccumulatedAmount() == null ? BigDecimal.ZERO : order.getAccumulatedAmount();
                int days = nz(order.getAccumulateDays()) + 1;
                update.setAccumulatedAmount(prev.add(paidPool));
                update.setAccumulateDays(Integer.valueOf(days));
                update.setLastAccumulateDate(today);
                if (order.getAccumulateCycleStartAt() == null)
                {
                    update.setAccumulateCycleStartAt(now);
                }
            }
        }
        update.setRemainingDays(Integer.valueOf(remainMax));
        if (allLotsDone)
        {
            update.setStatus(BizConstants.ORDER_FINISHED);
        }
        else if (!allActivated)
        {
            update.setRemainingDays(Integer.valueOf(Math.max(remainMax, 1)));
        }
        orderMapper.updateOrder(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizOrder settleAccumulate(Long memberId, Long orderId)
    {
        if (memberId == null || orderId == null)
        {
            throw new ServiceException("订单不存在");
        }
        BizOrder order = orderMapper.selectOrderById(orderId);
        if (order == null || !memberId.equals(order.getMemberId()))
        {
            throw new ServiceException("订单不存在");
        }
        if (order.protectIncome())
        {
            throw new ServiceException("累计收益在一拖二激活后自动转入产品收益，无需手动结算");
        }
        if (!order.accumulateIncome())
        {
            throw new ServiceException("该订单不是累计结算模式");
        }
        BigDecimal amount = order.getAccumulatedAmount() == null ? BigDecimal.ZERO : order.getAccumulatedAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("暂无可结算累计金额");
        }
        int cycle = nz(order.getAccumulateCycleDays());
        int days = nz(order.getAccumulateDays());
        if (cycle > 0 && days < cycle)
        {
            throw new ServiceException("未满累计周期（" + days + "/" + cycle + "天）");
        }
        String relatedName = StringUtils.isEmpty(order.getRelatedProductName()) ? "对档产品" : order.getRelatedProductName();
        if (order.getRelatedProductId() == null)
        {
            throw new ServiceException("请先认购" + relatedName + "后再结算");
        }

        RelatedSlotSnapshot slot = resolveRelatedSlots(memberId, order);
        int orderQty = qtyOf(order);
        int settledInCycle = nz(order.getAccumulateSettledShares());
        if (settledInCycle < 0)
        {
            settledInCycle = 0;
        }
        if (settledInCycle > orderQty)
        {
            settledInCycle = orderQty;
        }
        int remainShares = orderQty - settledInCycle;
        if (remainShares <= 0)
        {
            throw new ServiceException("本周期累计已全部结算，请等待下一周期");
        }
        int settleShares = Math.min(remainShares, slot.available);
        if (settleShares <= 0)
        {
            throw new ServiceException("需要已激活的" + relatedName
                    + "（已激活" + slot.activated + "份，本单可用" + slot.available + "份），本单本轮还需" + remainShares + "份");
        }

        BigDecimal credit = amount.multiply(new BigDecimal(settleShares))
                .divide(new BigDecimal(remainShares), 4, RoundingMode.HALF_UP);
        if (credit.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("暂无可结算累计金额");
        }
        if (credit.compareTo(amount) > 0)
        {
            credit = amount;
        }
        BigDecimal remainAmount = amount.subtract(credit);
        if (remainAmount.compareTo(BigDecimal.ZERO) < 0)
        {
            remainAmount = BigDecimal.ZERO;
        }

        String currency = StringUtils.isEmpty(order.getCurrency())
                ? BizConstants.CURRENCY_CNY : order.getCurrency().toUpperCase();
        walletService.credit(memberId, currency, credit, BizConstants.BIZ_ACCUMULATE_SETTLE,
                order.getOrderId(),
                "订单累计结算:" + order.getProductName() + "(" + settleShares + "/" + remainShares + "份)",
                BizConstants.WALLET_PRODUCT);

        int newSettledInCycle = settledInCycle + settleShares;
        int newSlotsUsed = nz(order.getRelatedSlotsUsed()) + settleShares;
        boolean cycleDone = newSettledInCycle >= orderQty || remainAmount.compareTo(BigDecimal.ZERO) <= 0;

        BizOrder patch = new BizOrder();
        patch.setOrderId(orderId);
        patch.setAccumulatedAmount(cycleDone ? BigDecimal.ZERO : remainAmount);
        patch.setRelatedSlotsUsed(Integer.valueOf(newSlotsUsed));
        if (cycleDone)
        {
            patch.setAccumulateDays(Integer.valueOf(0));
            patch.setAccumulatePaused("0");
            patch.setAccumulateSettledShares(Integer.valueOf(0));
            patch.setAccumulateCycleStartAt(DateUtils.getNowDate());
        }
        else
        {
            patch.setAccumulateSettledShares(Integer.valueOf(newSettledInCycle));
            patch.setAccumulatePaused("1");
        }
        orderMapper.updateOrder(patch);
        return selectOrderById(orderId);
    }

    /**
     * 对档可用结算份额（按「本单」计算，不同累计产品单互不抢份额）：
     * 可用 = 对档已激活份数 - 本单 related_slots_used。
     * 例：A 激活 1 份时，A1、A2 各可结一轮；A1 结完不影响 A2。
     * 同一单进入下一 60 天周期时，因本单已消耗，需更多已激活对档。
     * 激活口径与 App「已激活 x/y」一致（unlock lot 有 activate_time）；认购份数不计入。
     */
    private RelatedSlotSnapshot resolveRelatedSlots(Long memberId, BizOrder order)
    {
        RelatedSlotSnapshot snap = new RelatedSlotSnapshot();
        Long relatedProductId = order.getRelatedProductId();
        if (memberId == null || relatedProductId == null)
        {
            return snap;
        }
        snap.activated = countActivatedShares(memberId, relatedProductId);
        int usedByThisOrder = Math.max(0, nz(order.getRelatedSlotsUsed()));
        snap.available = Math.max(0, snap.activated - usedByThisOrder);
        return snap;
    }

    /**
     * 统计会员某产品已激活份数（与 fillActivate / App 已激活一致）。
     * 先跑 unlock plan 同步 lot，再按 activate_time 汇总；不用 order.quantity。
     */
    private int countActivatedShares(Long memberId, Long productId)
    {
        if (memberId == null || productId == null)
        {
            return 0;
        }
        UnlockSupport support = new UnlockSupport();
        support.plan(memberId, productId);

        BizOrder query = new BizOrder();
        query.setMemberId(memberId);
        query.setProductId(productId);
        List<BizOrder> list = orderMapper.selectOrderList(query);
        if (list == null || list.isEmpty())
        {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < list.size(); i++)
        {
            BizOrder row = fillActivate(list.get(i), support);
            if (row != null)
            {
                sum += nz(row.getActivatedQty());
            }
        }
        return Math.max(0, sum);
    }

    private static final class RelatedSlotSnapshot
    {
        private int activated;
        private int available;
    }

    private void fillAccumulateFlags(BizOrder order)
    {
        if (order == null)
        {
            return;
        }
        order.setProtectDays(Integer.valueOf(nz(order.getProtectDays())));
        if (order.protectIncome())
        {
            order.setCanSettleAccumulate(Boolean.FALSE);
            order.setRelatedProductOwned(Boolean.FALSE);
            order.setRelatedActivatedQty(Integer.valueOf(0));
            order.setRelatedSlotsAvailable(Integer.valueOf(0));
            order.setSettleableShares(Integer.valueOf(0));
            int duration = order.getDurationDays() == null ? 0 : order.getDurationDays().intValue();
            int remain = nz(order.getRemainingDays());
            int paidCount = duration > 0 ? Math.max(0, duration - remain) : 0;
            int protect = nz(order.getProtectDays());
            BigDecimal pool = order.getAccumulatedAmount() == null ? BigDecimal.ZERO : order.getAccumulatedAmount();
            boolean pastProtect = protect > 0 && paidCount >= protect;
            order.setAccumulateVisible(Boolean.valueOf(pastProtect || pool.compareTo(BigDecimal.ZERO) > 0));
            return;
        }
        if (!order.accumulateIncome())
        {
            order.setCanSettleAccumulate(Boolean.FALSE);
            order.setRelatedProductOwned(Boolean.FALSE);
            order.setRelatedActivatedQty(Integer.valueOf(0));
            order.setRelatedSlotsAvailable(Integer.valueOf(0));
            order.setSettleableShares(Integer.valueOf(0));
            order.setAccumulateVisible(Boolean.FALSE);
            return;
        }
        RelatedSlotSnapshot slot = resolveRelatedSlots(order.getMemberId(), order);
        order.setRelatedActivatedQty(Integer.valueOf(slot.activated));
        order.setRelatedSlotsAvailable(Integer.valueOf(slot.available));
        order.setRelatedProductOwned(Boolean.valueOf(slot.available > 0));
        order.setAccumulateVisible(Boolean.TRUE);

        BigDecimal amount = order.getAccumulatedAmount() == null ? BigDecimal.ZERO : order.getAccumulatedAmount();
        int cycle = nz(order.getAccumulateCycleDays());
        int days = nz(order.getAccumulateDays());
        boolean cycleOk = cycle <= 0 || days >= cycle;
        int orderQty = qtyOf(order);
        int settledInCycle = nz(order.getAccumulateSettledShares());
        if (settledInCycle < 0)
        {
            settledInCycle = 0;
        }
        if (settledInCycle > orderQty)
        {
            settledInCycle = orderQty;
        }
        int remainShares = Math.max(0, orderQty - settledInCycle);
        int settleShares = Math.min(remainShares, slot.available);
        order.setSettleableShares(Integer.valueOf(settleShares));
        order.setCanSettleAccumulate(Boolean.valueOf(
                amount.compareTo(BigDecimal.ZERO) > 0 && cycleOk && settleShares > 0));
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
        // 已激活 = 有 activate_time 的份数；未跑通一拖二的份额不会建 lot，买过≠激活
        int activated = 0;
        int ready = 0;
        Date nextStart = null;
        Date firstStart = null;
        boolean protect = order.protectIncome();
        int protectDays = nz(order.getProtectDays());
        int duration = order.getDurationDays() == null ? 0 : order.getDurationDays().intValue();
        for (int i = 0; i < lots.size(); i++)
        {
            BizOrderUnlockLot lot = lots.get(i);
            if (lot.getActivateTime() != null)
            {
                activated += qtyOfLot(lot);
            }
            Date start = lot.getIncomeStartTime();
            if (start != null && (firstStart == null || start.before(firstStart)))
            {
                firstStart = start;
            }
            if (protect)
            {
                int remain = nz(lot.getRemainingDays());
                int paidCount = duration > 0 ? Math.max(0, duration - remain) : 0;
                boolean inProtect = protectDays <= 0 || paidCount < protectDays;
                if (remain > 0 && (inProtect || lot.getActivateTime() != null))
                {
                    ready += qtyOfLot(lot);
                }
                continue;
            }
            if (lot.getActivateTime() == null)
            {
                continue;
            }
            if (start != null && !now.before(start))
            {
                ready += qtyOfLot(lot);
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
        if (protect)
        {
            if (order.getCreateTime() != null)
            {
                order.setIncomeStartTime(order.getCreateTime());
            }
        }
        else if (activated <= 0)
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

    private Date plusDays(Date time, int days)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DAY_OF_MONTH, days);
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

    /**
     * 新激活份数按「激活前未激活份数」比例，把累计池转入产品收益。
     * 产品结束后仍可转。当天日返尚未发放时，下次 rebate 会走产品收益。
     */
    private void transferAccumulateOnActivate(BizOrder order, int newlyActivated, int unactivatedBefore)
    {
        if (order == null || newlyActivated <= 0 || unactivatedBefore <= 0)
        {
            return;
        }
        BizOrder fresh = orderMapper.selectOrderById(order.getOrderId());
        if (fresh == null || !fresh.protectIncome())
        {
            return;
        }
        BigDecimal pool = fresh.getAccumulatedAmount() == null ? BigDecimal.ZERO : fresh.getAccumulatedAmount();
        if (pool.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        BigDecimal credit = pool.multiply(new BigDecimal(newlyActivated))
                .divide(new BigDecimal(unactivatedBefore), 4, RoundingMode.HALF_UP);
        if (credit.compareTo(pool) > 0)
        {
            credit = pool;
        }
        if (credit.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        String currency = StringUtils.isEmpty(fresh.getCurrency())
                ? BizConstants.CURRENCY_CNY : fresh.getCurrency().toUpperCase();
        walletService.credit(fresh.getMemberId(), currency, credit, BizConstants.BIZ_ACCUMULATE_SETTLE,
                fresh.getOrderId(), "累计收益转入产品收益:" + fresh.getProductName(),
                BizConstants.WALLET_PRODUCT);
        BigDecimal remain = pool.subtract(credit);
        if (remain.compareTo(BigDecimal.ZERO) < 0)
        {
            remain = BigDecimal.ZERO;
        }
        BizOrder patch = new BizOrder();
        patch.setOrderId(fresh.getOrderId());
        patch.setAccumulatedAmount(remain);
        if (remain.compareTo(BigDecimal.ZERO) <= 0)
        {
            patch.setAccumulateDays(Integer.valueOf(0));
        }
        orderMapper.updateOrder(patch);
        order.setAccumulatedAmount(remain);
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
                if (productId.equals(row.getProductId())
                        && (row.protectIncome() || BizConstants.ORDER_HOLDING.equals(row.getStatus())))
                {
                    parents.add(row);
                }
            }
            List<BizOrder> tierDowns = new ArrayList<BizOrder>();
            List<BizOrder> allDowns = downs(memberId);
            for (int i = 0; i < allDowns.size(); i++)
            {
                BizOrder row = allDowns.get(i);
                if (productId.equals(row.getProductId()))
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
            if (!order.protectIncome())
            {
                return syncCreditLots(order, activate, startIndex, need, orderNeed, tierDowns);
            }
            List<BizOrderUnlockLot> existing = lotMapper.selectByOrderId(order.getOrderId());
            if (existing == null)
            {
                existing = new ArrayList<BizOrderUnlockLot>();
            }
            Map<Integer, BizOrderUnlockLot> byShare = new HashMap<Integer, BizOrderUnlockLot>();
            int prevActivated = 0;
            int siblingRemain = -1;
            Date siblingLast = order.getLastRebateDate();
            for (int i = 0; i < existing.size(); i++)
            {
                BizOrderUnlockLot row = existing.get(i);
                byShare.put(Integer.valueOf(nz(row.getShareNo())), row);
                if (row.getActivateTime() != null)
                {
                    prevActivated += qtyOfLot(row);
                }
                if (siblingRemain < 0 && row.getRemainingDays() != null)
                {
                    siblingRemain = nz(row.getRemainingDays());
                }
                if (row.getLastRebateDate() != null)
                {
                    siblingLast = row.getLastRebateDate();
                }
            }
            Date ownTime = order.getCreateTime() != null ? order.getCreateTime() : DateUtils.getNowDate();
            int delay = nz(order.getUnlockDelayHours());
            int duration = order.getDurationDays() == null ? 0 : order.getDurationDays().intValue();
            int inheritRemain = siblingRemain >= 0 ? siblingRemain : nz(order.getRemainingDays());
            if (inheritRemain <= 0)
            {
                inheritRemain = duration;
            }
            int oQty = qtyOf(order);
            int newlyActivated = 0;
            for (int shareNo = 0; shareNo < oQty; shareNo++)
            {
                boolean shouldActivate = shareNo < activate;
                Date activateTime = null;
                if (shouldActivate)
                {
                    activateTime = ownTime;
                    if (orderNeed > 0 && startIndex >= 0)
                    {
                        Date reached = firstReachTime(tierDowns, (startIndex + shareNo + 1) * need);
                        if (reached == null)
                        {
                            shouldActivate = false;
                            activateTime = null;
                        }
                        else if (reached.after(ownTime))
                        {
                            activateTime = reached;
                        }
                    }
                }
                BizOrderUnlockLot lot = byShare.get(Integer.valueOf(shareNo));
                if (lot == null)
                {
                    lot = new BizOrderUnlockLot();
                    lot.setOrderId(order.getOrderId());
                    lot.setShareNo(Integer.valueOf(shareNo));
                    lot.setQty(Integer.valueOf(1));
                    lot.setActivateTime(activateTime);
                    lot.setIncomeStartTime(activateTime == null ? null : plusHours(activateTime, delay));
                    lot.setRemainingDays(Integer.valueOf(inheritRemain));
                    lot.setLastRebateDate(siblingLast);
                    lotMapper.insertLot(lot);
                    existing.add(lot);
                    byShare.put(Integer.valueOf(shareNo), lot);
                    if (activateTime != null)
                    {
                        newlyActivated += qtyOfLot(lot);
                    }
                }
                else if (shouldActivate && lot.getActivateTime() == null && activateTime != null)
                {
                    lot.setActivateTime(activateTime);
                    lot.setIncomeStartTime(plusHours(activateTime, delay));
                    lotMapper.updateLot(lot);
                    newlyActivated += qtyOfLot(lot);
                }
            }
            int unactivatedBefore = Math.max(0, oQty - prevActivated);
            if (newlyActivated > 0 && unactivatedBefore > 0)
            {
                transferAccumulateOnActivate(order, newlyActivated, unactivatedBefore);
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

        private List<BizOrderUnlockLot> syncCreditLots(BizOrder order, int activate, int startIndex, int need,
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
