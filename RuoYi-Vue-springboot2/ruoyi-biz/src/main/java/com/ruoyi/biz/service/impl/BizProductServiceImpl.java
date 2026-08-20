package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.mapper.BizProductMapper;
import com.ruoyi.biz.service.IBizProductService;

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
        if (product.getCurrency() == null || product.getCurrency().isEmpty())
        {
            product.setCurrency(BizConstants.CURRENCY_CNY);
        }
        else
        {
            product.setCurrency(product.getCurrency().toUpperCase());
        }
        return productMapper.insertProduct(product);
    }

    @Override
    public int updateProduct(BizProduct product)
    {
        if (product.getCurrency() != null && !product.getCurrency().isEmpty())
        {
            product.setCurrency(product.getCurrency().toUpperCase());
        }
        return productMapper.updateProduct(product);
    }

    @Override
    public int deleteProductByIds(Long[] productIds)
    {
        return productMapper.deleteProductByIds(productIds);
    }
}
