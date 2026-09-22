package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizProductCardTemplate;

public interface BizProductCardTemplateMapper
{
    BizProductCardTemplate selectTemplateById(Long templateId);

    BizProductCardTemplate selectTemplateByCode(String templateCode);

    List<BizProductCardTemplate> selectTemplateList(BizProductCardTemplate query);

    List<BizProductCardTemplate> selectEnabledOptions();

    int countProductByTemplateId(Long templateId);

    int insertTemplate(BizProductCardTemplate template);

    int updateTemplate(BizProductCardTemplate template);

    int deleteTemplateByIds(Long[] templateIds);
}
