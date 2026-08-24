package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppOkResult;
import com.ruoyi.biz.api.AppPayAccountListResult;
import com.ruoyi.biz.api.AppPayAccountResult;
import com.ruoyi.biz.domain.BizPayAccount;
import com.ruoyi.biz.service.IBizPayAccountService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.AppSecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-收款账户")
@RestController
@RequestMapping("/app")
public class AppPayAccountController extends BaseController
{
    @Autowired
    private IBizPayAccountService payAccountService;

    @ApiOperation(value = "我的收款账户", notes = "type 可选 USDT/BANK/ALIPAY。钱包管理页用这条。别名 /app/wallet/accounts。")
    @GetMapping({"/payAccounts", "/wallet/accounts"})
    public AppPayAccountListResult list(String type, String accountType)
    {
        String t = StringUtils.isEmpty(type) ? accountType : type;
        return AppPayAccountListResult.ok(payAccountService.selectMyAccounts(AppSecurityUtils.getMemberId(), t));
    }

    @ApiOperation(value = "保存收款账户", notes = "新增不传 accountId。每类最多 5 个。第一张自动默认。USDT 默认 TRC20。")
    @PostMapping({"/payAccounts", "/wallet/accounts"})
    public AppPayAccountResult save(@RequestBody BizPayAccount account)
    {
        return AppPayAccountResult.ok(payAccountService.saveMine(AppSecurityUtils.getMemberId(), account));
    }

    @ApiOperation("修改收款账户")
    @PutMapping({"/payAccounts/{accountId}", "/wallet/accounts/{accountId}"})
    public AppPayAccountResult edit(@PathVariable Long accountId, @RequestBody BizPayAccount account)
    {
        account.setAccountId(accountId);
        return AppPayAccountResult.ok(payAccountService.saveMine(AppSecurityUtils.getMemberId(), account));
    }

    @ApiOperation("删除收款账户")
    @DeleteMapping({"/payAccounts/{accountId}", "/wallet/accounts/{accountId}"})
    public AppOkResult remove(@PathVariable Long accountId)
    {
        payAccountService.deleteMine(AppSecurityUtils.getMemberId(), accountId);
        return AppOkResult.ok();
    }
}
