package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("官方群聊响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppGroupChatResult extends AppDataResult<java.util.List<AppGroupChatItem>>
{
    public static AppGroupChatResult ok(java.util.List<AppGroupChatItem> data)
    {
        return fillOk(new AppGroupChatResult(), data);
    }

    public static AppGroupChatResult fail(String message)
    {
        return fillFail(new AppGroupChatResult(), message);
    }
}
