package com.ruoyi.biz.service;

import com.ruoyi.biz.domain.BizAbout;

public interface IBizAboutService
{
    BizAbout getSingleton();

    int saveSingleton(BizAbout about);
}
