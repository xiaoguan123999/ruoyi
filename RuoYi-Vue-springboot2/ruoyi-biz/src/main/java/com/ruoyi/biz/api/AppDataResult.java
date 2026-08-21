package com.ruoyi.biz.api;

import io.swagger.annotations.ApiModelProperty;

public class AppDataResult<T> extends AppOkResult
{
    @ApiModelProperty(value = "业务数据", position = 3)
    private T data;

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    protected static <R extends AppDataResult<T>, T> R fillOk(R r, T data)
    {
        r.setCode(Integer.valueOf(200));
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    protected static <R extends AppDataResult<?>> R fillFail(R r, String message)
    {
        r.setCode(Integer.valueOf(500));
        r.setMsg(message);
        return r;
    }
}
