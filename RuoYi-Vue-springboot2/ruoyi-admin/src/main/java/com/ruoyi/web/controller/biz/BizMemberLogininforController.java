package com.ruoyi.web.controller.biz;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizMemberLogininfor;
import com.ruoyi.biz.service.IBizMemberLogininforService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-会员登录日志")
@RestController
@RequestMapping("/biz/memberLogin")
public class BizMemberLogininforController extends BaseController
{
    @Autowired
    private IBizMemberLogininforService logininforService;

    @ApiOperation("会员登录日志列表")
    @PreAuthorize("@ss.hasPermi('biz:memberLogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMemberLogininfor query)
    {
        startPage();
        List<BizMemberLogininfor> list = logininforService.selectLogininforList(query);
        return getDataTable(list);
    }

    @ApiOperation("导出会员登录日志")
    @Log(title = "会员登录日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('biz:memberLogin:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizMemberLogininfor query)
    {
        List<BizMemberLogininfor> list = logininforService.selectLogininforList(query);
        ExcelUtil<BizMemberLogininfor> util = new ExcelUtil<BizMemberLogininfor>(BizMemberLogininfor.class);
        util.exportExcel(response, list, "会员登录日志");
    }

    @ApiOperation("删除会员登录日志")
    @PreAuthorize("@ss.hasPermi('biz:memberLogin:remove')")
    @Log(title = "会员登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
    }

    @ApiOperation("清空会员登录日志")
    @PreAuthorize("@ss.hasPermi('biz:memberLogin:remove')")
    @Log(title = "会员登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        logininforService.cleanLogininfor();
        return success();
    }
}
