package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizProduct;

public interface BizProductMapper
{
    BizProduct selectProductById(Long productId);

    List<BizProduct> selectProductList(BizProduct product);

    int insertProduct(BizProduct product);

    int updateProduct(BizProduct product);

    int deleteProductByIds(Long[] productIds);

    int countByCategoryId(Long categoryId);
}
