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
import com.ruoyi.biz.domain.BizGroupChat;
import com.ruoyi.biz.service.IBizGroupChatService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-官方群聊")
@RestController
@RequestMapping("/biz/group")
public class BizGroupChatController extends BaseController
{
    @Autowired
    private IBizGroupChatService groupChatService;

    @ApiOperation("群聊列表")
    @PreAuthorize("@ss.hasPermi('biz:group:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizGroupChat groupChat)
    {
        startPage();
        List<BizGroupChat> list = groupChatService.selectGroupChatList(groupChat);
        return getDataTable(list);
    }

    @ApiOperation("群聊详情")
    @PreAuthorize("@ss.hasPermi('biz:group:query')")
    @GetMapping("/{groupId}")
    public AjaxResult getInfo(@PathVariable Long groupId)
    {
        return success(groupChatService.selectGroupChatById(groupId));
    }

    @ApiOperation("新增群聊")
    @PreAuthorize("@ss.hasPermi('biz:group:add')")
    @Log(title = "官方群聊", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizGroupChat groupChat)
    {
        groupChat.setCreateBy(getUsername());
        return toAjax(groupChatService.insertGroupChat(groupChat));
    }

    @ApiOperation("修改群聊")
    @PreAuthorize("@ss.hasPermi('biz:group:edit')")
    @Log(title = "官方群聊", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizGroupChat groupChat)
    {
        groupChat.setUpdateBy(getUsername());
        return toAjax(groupChatService.updateGroupChat(groupChat));
    }

    @ApiOperation("删除群聊")
    @PreAuthorize("@ss.hasPermi('biz:group:remove')")
    @Log(title = "官方群聊", businessType = BusinessType.DELETE)
    @DeleteMapping("/{groupIds}")
    public AjaxResult remove(@PathVariable Long[] groupIds)
    {
        return toAjax(groupChatService.deleteGroupChatByIds(groupIds));
    }
}
