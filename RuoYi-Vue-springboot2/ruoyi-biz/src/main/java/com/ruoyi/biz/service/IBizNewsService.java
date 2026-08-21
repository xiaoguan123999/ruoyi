package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.BizNews;

public interface IBizNewsService
{
    BizNews selectNewsById(Long newsId);

    List<BizNews> selectNewsList(BizNews news);

    List<BizNews> selectAppNewsList();

    int insertNews(BizNews news);

    int updateNews(BizNews news);

    int deleteNewsByIds(Long[] newsIds);
}
