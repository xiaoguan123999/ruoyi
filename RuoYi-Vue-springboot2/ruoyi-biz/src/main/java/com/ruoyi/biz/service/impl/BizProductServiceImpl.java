package com.ruoyi.biz.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.mapper.BizProductMapper;
import com.ruoyi.biz.service.IBizProductService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class BizProductServiceImpl implements IBizProductService
{
    @Autowired
    private BizProductMapper productMapper;

    @Override
    public BizProduct selectProductById(Long productId)
    {
        return productMapper.selectProductById(productId);
    }

    @Override
    public List<BizProduct> selectProductList(BizProduct product)
    {
        return productMapper.selectProductList(product);
    }

    @Override
    public int insertProduct(BizProduct product)
    {
        if (product.getStatus() == null)
        {
            product.setStatus(BizConstants.STATUS_OK);
        }
        if (product.getWithdrawRequired() == null)
        {
            product.setWithdrawRequired("0");
        }
        if (product.getNameEn() == null)
        {
            product.setNameEn("");
        }
        if (product.getCoverUrl() == null)
        {
            product.setCoverUrl("");
        }
        if (product.getBuyLimit() == null || product.getBuyLimit().intValue() < 0)
        {
            product.setBuyLimit(Integer.valueOf(0));
        }
        if (product.getUnlockDirectQty() == null || product.getUnlockDirectQty().intValue() < 0)
        {
            product.setUnlockDirectQty(Integer.valueOf(0));
        }
        if (product.getUnlockDelayHours() == null || product.getUnlockDelayHours().intValue() < 0)
        {
            product.setUnlockDelayHours(Integer.valueOf(0));
        }
        fillDualPrices(product);
        return productMapper.insertProduct(product);
    }

    @Override
    public int updateProduct(BizProduct product)
    {
        if (product.getUnlockDirectQty() != null && product.getUnlockDirectQty().intValue() < 0)
        {
            product.setUnlockDirectQty(Integer.valueOf(0));
        }
        if (product.getUnlockDelayHours() != null && product.getUnlockDelayHours().intValue() < 0)
        {
            product.setUnlockDelayHours(Integer.valueOf(0));
        }
        fillDualPrices(product);
        return productMapper.updateProduct(product);
    }

    @Override
    public int deleteProductByIds(Long[] productIds)
    {
        return productMapper.deleteProductByIds(productIds);
    }

    private void fillDualPrices(BizProduct product)
    {
        boolean cny = BizProduct.hasPrice(product.getPriceCny());
        boolean usdt = BizProduct.hasPrice(product.getPriceUsdt());
        if (!cny && !usdt)
        {
            throw new ServiceException("请至少配置人民币或USDT认购价格");
        }
        if (cny && product.getDailyRebateCny() == null)
        {
            product.setDailyRebateCny(BigDecimal.ZERO);
        }
        if (usdt && product.getDailyRebateUsdt() == null)
        {
            product.setDailyRebateUsdt(BigDecimal.ZERO);
        }
        if (cny)
        {
            product.setPrice(product.getPriceCny());
            product.setDailyRebate(product.getDailyRebateCny());
            product.setCurrency(BizConstants.CURRENCY_CNY);
        }
        else
        {
            product.setPrice(product.getPriceUsdt());
            product.setDailyRebate(product.getDailyRebateUsdt());
            product.setCurrency(BizConstants.CURRENCY_USDT);
        }
    }
}
