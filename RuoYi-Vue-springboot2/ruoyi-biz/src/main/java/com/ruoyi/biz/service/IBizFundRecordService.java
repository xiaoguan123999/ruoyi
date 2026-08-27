package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.api.AppFundRecordItem;

public interface IBizFundRecordService
{
    List<AppFundRecordItem> selectAppFundRecords(Long memberId, String currency, String bizType, String status);
}
