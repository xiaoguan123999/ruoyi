package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizOverview;

public interface IBizOverviewService
{
    BizOverview selectOverviewById(Long itemId);

    List<BizOverview> selectOverviewList(BizOverview overview);

    List<BizOverview> selectAppOverviewList();

    int insertOverview(BizOverview overview);

    int updateOverview(BizOverview overview);

    int deleteOverviewByIds(Long[] itemIds);
}
