package com.ruoyi.web.controller.biz.lottery;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizLotteryRecord;
import com.ruoyi.biz.service.IBizLotteryRecordService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-大转盘抽奖记录")
@RestController
@RequestMapping("/biz/lotteryRecord")
public class BizLotteryRecordController extends BaseController
{
    @Autowired
    private IBizLotteryRecordService lotteryRecordService;

    @ApiOperation("抽奖流水列表")
    @PreAuthorize("@ss.hasPermi('biz:lotteryRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLotteryRecord query)
    {
        startPage();
        List<BizLotteryRecord> list = lotteryRecordService.selectRecordList(query);
        return getDataTable(list);
    }
}
