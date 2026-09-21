package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizChainDeposit;
import com.ruoyi.biz.domain.BizChainDepositConfig;
import com.ruoyi.biz.service.IBizChainDepositService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-链上充值")
@RestController
@RequestMapping("/biz/chainDeposit")
public class BizChainDepositController extends BaseController
{
    @Autowired
    private IBizChainDepositService chainDepositService;

    @ApiOperation("链上充值网络配置，TRC20/BEP20 开关和系统收款地址。不在系统参数设置里展示")
    @PreAuthorize("@ss.hasPermi('biz:chainDeposit:list')")
    @GetMapping("/config")
    public AjaxResult config()
    {
        return success(chainDepositService.getAdminConfig());
    }

    @ApiOperation("保存链上充值网络配置")
    @PreAuthorize("@ss.hasPermi('biz:chainDeposit:list')")
    @Log(title = "链上充值配置", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult saveConfig(@RequestBody BizChainDepositConfig body)
    {
        chainDepositService.saveAdminConfig(body);
        return success();
    }

    @ApiOperation("链上充值列表，可按 network=TRC20|BEP20 过滤")
    @PreAuthorize("@ss.hasPermi('biz:chainDeposit:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizChainDeposit query)
    {
        startPage();
        List<BizChainDeposit> list = chainDepositService.selectList(query);
        return getDataTable(list);
    }

    @ApiOperation("立即扫链补单")
    @PreAuthorize("@ss.hasPermi('biz:chainDeposit:query')")
    @Log(title = "链上扫链", businessType = BusinessType.UPDATE)
    @PutMapping("/scan")
    public AjaxResult scan()
    {
        chainDepositService.scan();
        return success();
    }

    @ApiOperation("模拟到账，仅 mock=true")
    @PreAuthorize("@ss.hasPermi('biz:chainDeposit:simulate')")
    @Log(title = "链上模拟到账", businessType = BusinessType.UPDATE)
    @PutMapping("/simulate/{outTradeNo}")
    public AjaxResult simulate(@PathVariable String outTradeNo)
    {
        chainDepositService.simulatePaid(outTradeNo, getUsername());
        return success();
    }
}
