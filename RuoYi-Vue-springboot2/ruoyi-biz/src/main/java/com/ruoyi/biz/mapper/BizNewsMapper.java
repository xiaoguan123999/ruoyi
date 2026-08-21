package com.ruoyi.biz.mapper;

import java.util.List;
import com.ruoyi.biz.domain.BizNews;

public interface BizNewsMapper
{
    BizNews selectNewsById(Long newsId);

    List<BizNews> selectNewsList(BizNews news);

    int insertNews(BizNews news);

    int updateNews(BizNews news);

    int deleteNewsByIds(Long[] newsIds);
}
