package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizPayAccount;
import com.ruoyi.biz.service.IBizPayAccountService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-收款账户")
@RestController
@RequestMapping("/biz/payAccount")
public class BizPayAccountController extends BaseController
{
    @Autowired
    private IBizPayAccountService payAccountService;

    @ApiOperation("收款账户列表")
    @PreAuthorize("@ss.hasPermi('biz:payAccount:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizPayAccount query)
    {
        startPage();
        List<BizPayAccount> list = payAccountService.selectPayAccountList(query);
        return getDataTable(list);
    }

    @ApiOperation("收款账户详情")
    @PreAuthorize("@ss.hasPermi('biz:payAccount:query')")
    @GetMapping("/{accountId}")
    public AjaxResult getInfo(@PathVariable Long accountId)
    {
        return success(payAccountService.selectPayAccountById(accountId));
    }

    @ApiOperation("新增收款账户")
    @PreAuthorize("@ss.hasPermi('biz:payAccount:add')")
    @Log(title = "收款账户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizPayAccount account)
    {
        return toAjax(payAccountService.insertPayAccount(account));
    }

    @ApiOperation("修改收款账户")
    @PreAuthorize("@ss.hasPermi('biz:payAccount:edit')")
    @Log(title = "收款账户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizPayAccount account)
    {
        return toAjax(payAccountService.updatePayAccount(account));
    }

    @ApiOperation("删除收款账户")
    @PreAuthorize("@ss.hasPermi('biz:payAccount:remove')")
    @Log(title = "收款账户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{accountIds}")
    public AjaxResult remove(@PathVariable Long[] accountIds)
    {
        return toAjax(payAccountService.deletePayAccountByIds(accountIds));
    }
}
