package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizProductCardTemplate;
import com.ruoyi.biz.mapper.BizProductCardTemplateMapper;
import com.ruoyi.biz.service.IBizProductCardTemplateService;
import com.ruoyi.biz.support.ProductCardMetricSupport;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizProductCardTemplateServiceImpl implements IBizProductCardTemplateService
{
    @Autowired
    private BizProductCardTemplateMapper templateMapper;

    @Override
    public BizProductCardTemplate selectTemplateById(Long templateId)
    {
        return templateMapper.selectTemplateById(templateId);
    }

    @Override
    public List<BizProductCardTemplate> selectTemplateList(BizProductCardTemplate query)
    {
        return templateMapper.selectTemplateList(query);
    }

    @Override
    public List<BizProductCardTemplate> selectEnabledOptions()
    {
        return templateMapper.selectEnabledOptions();
    }

    @Override
    public int insertTemplate(BizProductCardTemplate template)
    {
        fillDefaults(template);
        checkRequired(template, true);
        BizProductCardTemplate exists = templateMapper.selectTemplateByCode(template.getTemplateCode());
        if (exists != null)
        {
            throw new ServiceException("模板编码已存在：" + template.getTemplateCode());
        }
        return templateMapper.insertTemplate(template);
    }

    @Override
    public int updateTemplate(BizProductCardTemplate template)
    {
        fillDefaults(template);
        checkRequired(template, false);
        return templateMapper.updateTemplate(template);
    }

    @Override
    public int deleteTemplateByIds(Long[] templateIds)
    {
        if (templateIds == null || templateIds.length == 0)
        {
            return 0;
        }
        for (int i = 0; i < templateIds.length; i++)
        {
            Long id = templateIds[i];
            int used = templateMapper.countProductByTemplateId(id);
            if (used > 0)
            {
                BizProductCardTemplate t = templateMapper.selectTemplateById(id);
                String name = t != null ? t.getTemplateName() : String.valueOf(id);
                throw new ServiceException("模板「" + name + "」仍被产品引用，请先停用或改产品模板后再删除");
            }
        }
        return templateMapper.deleteTemplateByIds(templateIds);
    }

    private void fillDefaults(BizProductCardTemplate template)
    {
        if (StringUtils.isNotEmpty(template.getTemplateCode()))
        {
            template.setTemplateCode(ProductCardMetricSupport.normalizeCode(template.getTemplateCode()));
        }
        if (template.getPreviewUrl() == null)
        {
            template.setPreviewUrl("");
        }
        if (template.getHasMainAmount() == null || (!"0".equals(template.getHasMainAmount()) && !"1".equals(template.getHasMainAmount())))
        {
            template.setHasMainAmount("0");
        }
        if (StringUtils.isEmpty(template.getDefaultCtaText()))
        {
            template.setDefaultCtaText("立即参与");
        }
        if (template.getSort() == null)
        {
            template.setSort(Integer.valueOf(0));
        }
        if (StringUtils.isEmpty(template.getStatus()))
        {
            template.setStatus(BizConstants.STATUS_OK);
        }
        if (template.getMetricSlotCount() == null || template.getMetricSlotCount().intValue() < 0)
        {
            template.setMetricSlotCount(Integer.valueOf(2));
        }
    }

    private void checkRequired(BizProductCardTemplate template, boolean create)
    {
        if (create && StringUtils.isEmpty(template.getTemplateCode()))
        {
            throw new ServiceException("请填写模板编码");
        }
        if (StringUtils.isEmpty(template.getTemplateName()))
        {
            throw new ServiceException("请填写模板名称");
        }
    }
}
