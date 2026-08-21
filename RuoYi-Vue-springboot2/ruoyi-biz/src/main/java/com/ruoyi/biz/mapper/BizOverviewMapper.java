package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizOverview;

public interface BizOverviewMapper
{
    BizOverview selectOverviewById(Long itemId);

    BizOverview selectOverviewByItemKey(String itemKey);

    List<BizOverview> selectOverviewList(BizOverview overview);

    int insertOverview(BizOverview overview);

    int updateOverview(BizOverview overview);

    int deleteOverviewByIds(Long[] itemIds);
}
