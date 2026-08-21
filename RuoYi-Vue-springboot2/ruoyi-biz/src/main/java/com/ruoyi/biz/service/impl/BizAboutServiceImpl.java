package com.ruoyi.biz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizAbout;
import com.ruoyi.biz.mapper.BizAboutMapper;
import com.ruoyi.biz.service.IBizAboutService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizAboutServiceImpl implements IBizAboutService
{
    @Autowired
    private BizAboutMapper aboutMapper;

    @Override
    public BizAbout selectAboutById(Long aboutId)
    {
        return aboutMapper.selectAboutById(aboutId);
    }

    @Override
    public List<BizAbout> selectAboutList(BizAbout about)
    {
        return aboutMapper.selectAboutList(about);
    }

    @Override
    public List<BizAbout> selectAppAboutList()
    {
        BizAbout query = new BizAbout();
        query.setStatus(BizConstants.STATUS_OK);
        return aboutMapper.selectAboutList(query);
    }

    @Override
    public int insertAbout(BizAbout about)
    {
        fillDefaults(about);
        checkRequired(about);
        return aboutMapper.insertAbout(about);
    }

    @Override
    public int updateAbout(BizAbout about)
    {
        fillDefaults(about);
        checkRequired(about);
        return aboutMapper.updateAbout(about);
    }

    @Override
    public int deleteAboutByIds(Long[] aboutIds)
    {
        return aboutMapper.deleteAboutByIds(aboutIds);
    }

    private void fillDefaults(BizAbout about)
    {
        if (StringUtils.isEmpty(about.getStatus()))
        {
            about.setStatus(BizConstants.STATUS_OK);
        }
        if (about.getSubtitle() == null)
        {
            about.setSubtitle("");
        }
        if (about.getImageUrl() == null)
        {
            about.setImageUrl("");
        }
        if (about.getContent() == null)
        {
            about.setContent("");
        }
        if (about.getSort() == null)
        {
            about.setSort(0);
        }
    }

    private void checkRequired(BizAbout about)
    {
        if (StringUtils.isEmpty(about.getTitle()))
        {
            throw new ServiceException("请填写标题");
        }
        if (StringUtils.isEmpty(stripHtml(about.getContent())))
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
