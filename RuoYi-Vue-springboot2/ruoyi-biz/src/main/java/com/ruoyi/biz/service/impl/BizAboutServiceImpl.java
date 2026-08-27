package com.ruoyi.biz.service.impl;

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
    public BizAbout getSingleton()
    {
        BizAbout about = aboutMapper.selectAboutSingleton();
        if (about != null)
        {
            fillDefaults(about);
            return about;
        }
        about = new BizAbout();
        about.setTitle("星帆智联");
        about.setSubtitle("连接星空 · 智联未来");
        about.setContent("<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>");
        about.setImageUrl("");
        about.setMode(BizConstants.ABOUT_MODE_TEXT);
        about.setPdfUrl("");
        about.setSort(Integer.valueOf(1));
        about.setStatus(BizConstants.STATUS_OK);
        about.setCreateBy("admin");
        aboutMapper.insertAbout(about);
        return aboutMapper.selectAboutSingleton();
    }

    @Override
    public int saveSingleton(BizAbout about)
    {
        fillDefaults(about);
        checkRequired(about);
        BizAbout current = aboutMapper.selectAboutSingleton();
        if (current == null)
        {
            if (StringUtils.isEmpty(about.getCreateBy()))
            {
                about.setCreateBy("admin");
            }
            return aboutMapper.insertAbout(about);
        }
        about.setAboutId(current.getAboutId());
        return aboutMapper.updateAbout(about);
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
        if (about.getPdfUrl() == null)
        {
            about.setPdfUrl("");
        }
        if (about.getSort() == null)
        {
            about.setSort(Integer.valueOf(1));
        }
        String mode = about.getMode() == null ? "" : about.getMode().trim().toUpperCase();
        if (!BizConstants.ABOUT_MODE_PDF.equals(mode))
        {
            mode = BizConstants.ABOUT_MODE_TEXT;
        }
        about.setMode(mode);
    }

    private void checkRequired(BizAbout about)
    {
        if (BizConstants.ABOUT_MODE_PDF.equals(about.getMode()))
        {
            if (StringUtils.isEmpty(about.getPdfUrl()))
            {
                throw new ServiceException("请上传 PDF 文件");
            }
            return;
        }
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
