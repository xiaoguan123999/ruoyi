package com.ruoyi.web.controller.biz;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppTeamData;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizTeamRelationRow;
import com.ruoyi.biz.domain.BizTeamTreeNode;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-团队")
@RestController
@RequestMapping("/biz/team")
public class BizTeamController extends BaseController
{
    @Autowired
    private IBizMemberService memberService;

    @ApiOperation("团队成员列表")
    @PreAuthorize("@ss.hasPermi('biz:team:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMember member)
    {
        startPage();
        List<BizMember> list = memberService.selectMemberList(member);
        return getDataTable(list);
    }

    @ApiOperation("团队汇总")
    @PreAuthorize("@ss.hasPermi('biz:team:list')")
    @GetMapping("/summary/{memberId}")
    public AjaxResult summary(@PathVariable Long memberId)
    {
        BizMember member = memberService.selectMemberById(memberId);
        if (member == null)
        {
            return error("会员不存在");
        }
        AppTeamData team = memberService.getAppTeam(memberId);
        AjaxResult ajax = success();
        ajax.put("member", member);
        ajax.put("summary", team == null ? null : team.getSummary());
        return ajax;
    }

    @ApiOperation("会员结构图根节点")
    @PreAuthorize("@ss.hasPermi('biz:team:tree')")
    @GetMapping("/tree")
    public AjaxResult tree(@RequestParam("keyword") String keyword)
    {
        BizTeamTreeNode root = memberService.selectTeamTreeRoot(keyword);
        if (root == null)
        {
            return error("会员不存在");
        }
        return success(root);
    }

    @ApiOperation("会员结构图直推下级")
    @PreAuthorize("@ss.hasPermi('biz:team:tree')")
    @GetMapping("/children/{memberId}")
    public AjaxResult children(@PathVariable Long memberId)
    {
        return success(memberService.selectTeamTreeChildren(memberId));
    }

    @ApiOperation("推荐关系图：从顶点到该会员的路径")
    @PreAuthorize("@ss.hasPermi('biz:team:relation')")
    @GetMapping("/relation")
    public AjaxResult relation(@RequestParam("keyword") String keyword)
    {
        List<BizTeamRelationRow> rows = memberService.selectRecommendRelation(keyword);
        return success(rows);
    }

    @ApiOperation("导出推荐关系图")
    @PreAuthorize("@ss.hasPermi('biz:team:export')")
    @Log(title = "推荐关系图", businessType = BusinessType.EXPORT)
    @PostMapping("/relation/export")
    public void exportRelation(HttpServletResponse response, String keyword)
    {
        List<BizTeamRelationRow> rows = memberService.selectRecommendRelation(keyword);
        ExcelUtil<BizTeamRelationRow> util = new ExcelUtil<BizTeamRelationRow>(BizTeamRelationRow.class);
        util.exportExcel(response, rows, "推荐关系图");
    }
}
