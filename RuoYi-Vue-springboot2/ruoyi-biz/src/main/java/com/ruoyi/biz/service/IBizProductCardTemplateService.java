package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizProductCardTemplate;

public interface IBizProductCardTemplateService
{
    BizProductCardTemplate selectTemplateById(Long templateId);

    List<BizProductCardTemplate> selectTemplateList(BizProductCardTemplate query);

    List<BizProductCardTemplate> selectEnabledOptions();

    int insertTemplate(BizProductCardTemplate template);

    int updateTemplate(BizProductCardTemplate template);

    int deleteTemplateByIds(Long[] templateIds);
}
