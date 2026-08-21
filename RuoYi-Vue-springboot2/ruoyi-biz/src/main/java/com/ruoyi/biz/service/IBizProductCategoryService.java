package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizProductCategory;

public interface IBizProductCategoryService
{
    BizProductCategory selectCategoryById(Long categoryId);

    List<BizProductCategory> selectCategoryList(BizProductCategory category);

    List<BizProductCategory> selectAppSeriesList();

    int insertCategory(BizProductCategory category);

    int updateCategory(BizProductCategory category);

    int deleteCategoryByIds(Long[] categoryIds);
}
