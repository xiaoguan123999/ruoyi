package com.ruoyi.biz.mapper;


import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizLotteryChanceLog;

@Mapper
public interface BizLotteryChanceLogMapper
{
    BizLotteryChanceLog selectByBizNo(String bizNo);

    List<BizLotteryChanceLog> selectChanceLogList(BizLotteryChanceLog query);

    int insertChanceLog(BizLotteryChanceLog log);
}
