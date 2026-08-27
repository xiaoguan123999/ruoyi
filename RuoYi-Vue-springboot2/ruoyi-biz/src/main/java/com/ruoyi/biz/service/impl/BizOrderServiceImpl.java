package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizOrder;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.domain.BizRebateLog;
import com.ruoyi.biz.mapper.BizOrderMapper;
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
        return fillActivate(orderMapper.selectOrderById(orderId));
    }

    @Override
    public List<BizOrder> selectOrderList(BizOrder order)
    {
        List<BizOrder> list = orderMapper.selectOrderList(order);
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                fillActivate(list.get(i));
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
        refreshUnlockForMemberProduct(memberId, productId);
        if (member.getParentId() != null)
        {
            refreshUnlockForMemberProduct(member.getParentId(), productId);
        }
        return fillActivate(orderMapper.selectOrderById(order.getOrderId()));
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
        for (BizOrder order : orders)
        {
            try
            {
                SpringUtils.getAopProxy(this).rebateOne(order, today);
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
        if (order.getLastRebateDate() != null && !today.after(order.getLastRebateDate()))
        {
            return;
        }
        if (order.getRemainingDays() == null || order.getRemainingDays() <= 0)
        {
            return;
        }
        persistIncomeStartIfReady(order);
        fillActivate(order);
        if (!"1".equals(order.getActivateStatus()))
        {
            return;
        }
        String currency = StringUtils.isEmpty(order.getCurrency())
                ? BizConstants.CURRENCY_CNY : order.getCurrency().toUpperCase();
        walletService.credit(order.getMemberId(), currency, order.getDailyRebate(),
                BizConstants.BIZ_REBATE, order.getOrderId(), "产品每日返利");
        BizRebateLog rebateLog = new BizRebateLog();
        rebateLog.setOrderId(order.getOrderId());
        rebateLog.setMemberId(order.getMemberId());
        rebateLog.setCurrency(currency);
        rebateLog.setAmount(order.getDailyRebate());
        rebateLog.setRebateDate(today);
        rebateLogMapper.insertRebateLog(rebateLog);

        int remaining = order.getRemainingDays() - 1;
        BizOrder update = new BizOrder();
        update.setOrderId(order.getOrderId());
        update.setRemainingDays(remaining);
        update.setLastRebateDate(today);
        if (remaining <= 0)
        {
            update.setStatus(BizConstants.ORDER_FINISHED);
        }
        orderMapper.updateOrder(update);
    }

    private void refreshUnlockForMemberProduct(Long memberId, Long productId)
    {
        if (memberId == null || productId == null)
        {
            return;
        }
        BizOrder query = new BizOrder();
        query.setMemberId(memberId);
        query.setProductId(productId);
        query.setStatus(BizConstants.ORDER_HOLDING);
        List<BizOrder> list = orderMapper.selectOrderList(query);
        if (list == null)
        {
            return;
        }
        for (int i = 0; i < list.size(); i++)
        {
            persistIncomeStartIfReady(list.get(i));
        }
    }

    private void persistIncomeStartIfReady(BizOrder order)
    {
        if (order == null || order.getIncomeStartTime() != null)
        {
            return;
        }
        Date start = computeIncomeStart(order);
        if (start == null)
        {
            return;
        }
        BizOrder update = new BizOrder();
        update.setOrderId(order.getOrderId());
        update.setIncomeStartTime(start);
        orderMapper.updateOrder(update);
        order.setIncomeStartTime(start);
    }

    private BizOrder fillActivate(BizOrder order)
    {
        if (order == null)
        {
            return null;
        }
        persistIncomeStartIfReady(order);
        int need = nz(order.getUnlockDirectQty());
        int delay = nz(order.getUnlockDelayHours());
        if (need > 0)
        {
            order.setUnlockDirectHave(Integer.valueOf(sumDirectDownlineQty(order.getMemberId(), order.getProductId())));
        }
        else
        {
            order.setUnlockDirectHave(Integer.valueOf(0));
        }
        if (need <= 0 && delay <= 0)
        {
            order.setActivateStatus("1");
            return order;
        }
        Date start = order.getIncomeStartTime();
        Date now = DateUtils.getNowDate();
        if (start != null && !now.before(start))
        {
            order.setActivateStatus("1");
        }
        else
        {
            order.setActivateStatus("0");
        }
        return order;
    }

    private Date computeIncomeStart(BizOrder order)
    {
        int need = nz(order.getUnlockDirectQty());
        int delay = nz(order.getUnlockDelayHours());
        if (need <= 0 && delay <= 0)
        {
            return null;
        }
        Date ownTime = order.getCreateTime() != null ? order.getCreateTime() : DateUtils.getNowDate();
        Date conditionTime = ownTime;
        if (need > 0)
        {
            Date reached = firstReachTime(order.getMemberId(), order.getProductId(), need);
            if (reached == null)
            {
                return null;
            }
            if (reached.after(ownTime))
            {
                conditionTime = reached;
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(conditionTime);
        calendar.add(Calendar.HOUR_OF_DAY, delay);
        return calendar.getTime();
    }

    private Date firstReachTime(Long memberId, Long productId, int need)
    {
        List<BizOrder> downs = orderMapper.selectDirectDownlineProductOrders(memberId, productId);
        if (downs == null || downs.isEmpty())
        {
            return null;
        }
        int acc = 0;
        for (int i = 0; i < downs.size(); i++)
        {
            BizOrder row = downs.get(i);
            acc += nz(row.getQuantity()) <= 0 ? 1 : nz(row.getQuantity());
            if (acc >= need)
            {
                return row.getCreateTime() != null ? row.getCreateTime() : DateUtils.getNowDate();
            }
        }
        return null;
    }

    private int sumDirectDownlineQty(Long memberId, Long productId)
    {
        List<BizOrder> downs = orderMapper.selectDirectDownlineProductOrders(memberId, productId);
        if (downs == null)
        {
            return 0;
        }
        int acc = 0;
        for (int i = 0; i < downs.size(); i++)
        {
            int qty = nz(downs.get(i).getQuantity());
            acc += qty <= 0 ? 1 : qty;
        }
        return acc;
    }

    private int nz(Integer value)
    {
        return value == null ? 0 : value.intValue();
    }
}
