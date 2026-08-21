package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizProductCategory;
import com.ruoyi.biz.mapper.BizProductCategoryMapper;
import com.ruoyi.biz.mapper.BizProductMapper;
import com.ruoyi.biz.service.IBizProductCategoryService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizProductCategoryServiceImpl implements IBizProductCategoryService
{
    @Autowired
    private BizProductCategoryMapper categoryMapper;

    @Autowired
    private BizProductMapper productMapper;

    @Override
    public BizProductCategory selectCategoryById(Long categoryId)
    {
        return categoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public List<BizProductCategory> selectCategoryList(BizProductCategory category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    @Override
    public List<BizProductCategory> selectAppSeriesList()
    {
        BizProductCategory query = new BizProductCategory();
        query.setStatus(BizConstants.STATUS_OK);
        return categoryMapper.selectCategoryList(query);
    }

    @Override
    public int insertCategory(BizProductCategory category)
    {
        fillDefaults(category);
        checkRequired(category);
        return categoryMapper.insertCategory(category);
    }

    @Override
    public int updateCategory(BizProductCategory category)
    {
        fillDefaults(category);
        checkRequired(category);
        return categoryMapper.updateCategory(category);
    }

    @Override
    public int deleteCategoryByIds(Long[] categoryIds)
    {
        if (categoryIds != null)
        {
            for (int i = 0; i < categoryIds.length; i++)
            {
                int used = productMapper.countByCategoryId(categoryIds[i]);
                if (used > 0)
                {
                    throw new ServiceException("分类下还有产品，不能删除");
                }
            }
        }
        return categoryMapper.deleteCategoryByIds(categoryIds);
    }

    private void fillDefaults(BizProductCategory category)
    {
        if (StringUtils.isEmpty(category.getStatus()))
        {
            category.setStatus(BizConstants.STATUS_OK);
        }
        if (category.getCoverUrl() == null)
        {
            category.setCoverUrl("");
        }
        if (category.getSort() == null)
        {
            category.setSort(Integer.valueOf(0));
        }
    }

    private void checkRequired(BizProductCategory category)
    {
        if (StringUtils.isEmpty(category.getCategoryName()))
        {
            throw new ServiceException("请填写系列名称");
        }
    }
}
