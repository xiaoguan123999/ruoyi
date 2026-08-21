package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("登录/注册成功")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLoginResult extends AppOkResult
{
    @ApiModelProperty(value = "会员 token，后续请求放 Header：Authorization: Bearer <token>")
    private String token;
    @ApiModelProperty(value = "会员ID", example = "10001")
    private Long memberId;
    @ApiModelProperty(value = "7位邀请码", example = "5839201")
    private String inviteCode;
    @ApiModelProperty(value = "是否已绑定谷歌验证器")
    private Boolean gaBound;

    public static AppLoginResult of(String token, Long memberId, String inviteCode, boolean gaBound)
    {
        AppLoginResult r = new AppLoginResult();
        r.setCode(Integer.valueOf(200));
        r.setMsg("操作成功");
        r.token = token;
        r.memberId = memberId;
        r.inviteCode = inviteCode;
        r.gaBound = Boolean.valueOf(gaBound);
        return r;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public Boolean getGaBound() { return gaBound; }
    public void setGaBound(Boolean gaBound) { this.gaBound = gaBound; }
}
