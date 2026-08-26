package com.ruoyi.biz.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.domain.BizMemberLogininfor;
import com.ruoyi.biz.mapper.BizMemberLogininforMapper;
import com.ruoyi.biz.service.IBizMemberLogininforService;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.UserAgentUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;

@Service
public class BizMemberLogininforServiceImpl implements IBizMemberLogininforService
{
    private static final Logger log = LoggerFactory.getLogger(BizMemberLogininforServiceImpl.class);

    @Autowired
    private BizMemberLogininforMapper logininforMapper;

    @Override
    public void record(String phone, Long memberId, String status, String message)
    {
        try
        {
            String userAgent = "";
            if (ServletUtils.getRequest() != null)
            {
                userAgent = ServletUtils.getRequest().getHeader("User-Agent");
            }
            String ip = IpUtils.getIpAddr();
            BizMemberLogininfor row = new BizMemberLogininfor();
            row.setMemberId(memberId);
            row.setPhone(phone == null ? "" : phone);
            row.setIpaddr(ip);
            row.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
            row.setBrowser(UserAgentUtils.getBrowser(userAgent));
            row.setOs(UserAgentUtils.getOperatingSystem(userAgent));
            row.setMsg(message);
            if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
            {
                row.setStatus(Constants.SUCCESS);
            }
            else
            {
                row.setStatus(Constants.FAIL);
            }
            logininforMapper.insertLogininfor(row);
        }
        catch (Exception e)
        {
            log.error("member login log failed", e);
        }
    }

    @Override
    public List<BizMemberLogininfor> selectLogininforList(BizMemberLogininfor query)
    {
        return logininforMapper.selectLogininforList(query);
    }

    @Override
    public int deleteLogininforByIds(Long[] infoIds)
    {
        return logininforMapper.deleteLogininforByIds(infoIds);
    }

    @Override
    public void cleanLogininfor()
    {
        logininforMapper.cleanLogininfor();
    }
}
