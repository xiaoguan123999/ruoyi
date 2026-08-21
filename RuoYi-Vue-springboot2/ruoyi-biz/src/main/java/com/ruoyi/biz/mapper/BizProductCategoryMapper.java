package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizProductCategory;

public interface BizProductCategoryMapper
{
    BizProductCategory selectCategoryById(Long categoryId);

    List<BizProductCategory> selectCategoryList(BizProductCategory category);

    int insertCategory(BizProductCategory category);

    int updateCategory(BizProductCategory category);

    int deleteCategoryByIds(Long[] categoryIds);
}
