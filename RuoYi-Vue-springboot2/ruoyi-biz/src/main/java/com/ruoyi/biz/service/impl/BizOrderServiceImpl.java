package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
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

    @Override
    public BizOrder selectOrderById(Long orderId)
    {
        return orderMapper.selectOrderById(orderId);
    }

    @Override
    public List<BizOrder> selectOrderList(BizOrder order)
    {
        return orderMapper.selectOrderList(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizOrder subscribe(Long memberId, Long productId, String payCurrency, String payPassword)
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
        String currency = resolvePayCurrency(product, payCurrency);
        configService.assertCurrencyEnabled(currency);
        BigDecimal price = product.priceOf(currency);
        if (!BizProduct.hasPrice(price))
        {
            throw new ServiceException("USDT".equals(currency) ? "该产品不支持USDT认购" : "该产品不支持人民币认购");
        }
        BigDecimal rebate = product.rebateOf(currency);
        if (rebate == null)
        {
            rebate = BigDecimal.ZERO;
        }

        walletService.debit(memberId, currency, price, BizConstants.BIZ_SUBSCRIBE,
                productId, "认购产品:" + product.getProductName());

        BizOrder order = new BizOrder();
        order.setOrderNo(DateUtils.dateTimeNow() + IdUtils.fastSimpleUUID().substring(0, 8));
        order.setMemberId(memberId);
        order.setProductId(product.getProductId());
        order.setProductName(product.getProductName());
        order.setCurrency(currency);
        order.setPrice(price);
        order.setDailyRebate(rebate);
        order.setDurationDays(product.getDurationDays());
        order.setRemainingDays(product.getDurationDays());
        order.setWithdrawRequired(product.getWithdrawRequired());
        order.setStatus(BizConstants.ORDER_HOLDING);
        orderMapper.insertOrder(order);

        memberService.refreshLevel(memberId);
        if (member.getParentId() != null)
        {
            memberService.refreshLevel(member.getParentId());
        }
        BizOrder created = orderMapper.selectOrderById(order.getOrderId());
        return created != null ? created : order;
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
}
