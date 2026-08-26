package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizPayProvider;

public interface BizPayProviderMapper
{
    BizPayProvider selectPayProviderById(Long providerId);
    BizPayProvider selectPayProviderByCode(String providerCode);
    List<BizPayProvider> selectPayProviderList(BizPayProvider query);
    int updatePayProvider(BizPayProvider row);
}
