package com.ruoyi.biz.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizNews;
import com.ruoyi.biz.mapper.BizNewsMapper;
import com.ruoyi.biz.service.IBizNewsService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizNewsServiceImpl implements IBizNewsService
{
    @Autowired
    private BizNewsMapper newsMapper;

    @Override
    public BizNews selectNewsById(Long newsId)
    {
        return newsMapper.selectNewsById(newsId);
    }

    @Override
    public List<BizNews> selectNewsList(BizNews news)
    {
        return newsMapper.selectNewsList(news);
    }

    @Override
    public List<BizNews> selectAppNewsList()
    {
        BizNews query = new BizNews();
        query.setStatus(BizConstants.STATUS_OK);
        return newsMapper.selectNewsList(query);
    }

    @Override
    public int insertNews(BizNews news)
    {
        fillDefaults(news);
        checkRequired(news);
        return newsMapper.insertNews(news);
    }

    @Override
    public int updateNews(BizNews news)
    {
        fillDefaults(news);
        checkRequired(news);
        return newsMapper.updateNews(news);
    }

    @Override
    public int deleteNewsByIds(Long[] newsIds)
    {
        return newsMapper.deleteNewsByIds(newsIds);
    }

    private void fillDefaults(BizNews news)
    {
        if (StringUtils.isEmpty(news.getStatus()))
        {
            news.setStatus(BizConstants.STATUS_OK);
        }
        if (news.getSummary() == null)
        {
            news.setSummary("");
        }
        if (StringUtils.isEmpty(news.getSummary()) && StringUtils.isNotEmpty(news.getTitle()))
        {
            news.setSummary(news.getTitle());
        }
        if (news.getCoverUrl() == null)
        {
            news.setCoverUrl("");
        }
        if (news.getContent() == null)
        {
            news.setContent("");
        }
        if (news.getPublishTime() == null)
        {
            news.setPublishTime(new Date());
        }
        if (news.getSort() == null)
        {
            news.setSort(0);
        }
    }

    private void checkRequired(BizNews news)
    {
        if (StringUtils.isEmpty(news.getTitle()))
        {
            throw new ServiceException("请填写标题");
        }
        if (StringUtils.isEmpty(stripHtml(news.getContent())))
        {
            throw new ServiceException("请填写正文");
        }
    }

    private String stripHtml(String html)
    {
        if (StringUtils.isEmpty(html))
        {
            return "";
        }
        return html.replaceAll("(?i)<[^>]+>", "").replace("&nbsp;", " ").trim();
    }
}
