package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizProduct;

public interface IBizProductService
{
    BizProduct selectProductById(Long productId);

    List<BizProduct> selectProductList(BizProduct product);

    /** App 列表/详情：补齐 layoutType、metrics.display、主金额文案等 */
    void enrichForApp(List<BizProduct> products);

    void enrichForApp(BizProduct product);

    int insertProduct(BizProduct product);

    int updateProduct(BizProduct product);

    int deleteProductByIds(Long[] productIds);
}
