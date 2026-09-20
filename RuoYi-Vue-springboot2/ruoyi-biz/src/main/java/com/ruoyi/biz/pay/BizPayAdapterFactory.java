package com.ruoyi.biz.pay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizPayProvider;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Component
public class BizPayAdapterFactory
{
    @Autowired
    private MockPayAdapter mockPayAdapter;

    @Autowired
    private MonPayAdapter monPayAdapter;

    @Autowired
    private WuYouAdapter wuYouAdapter;

    @Autowired
    private JeepayAdapter jeepayAdapter;

    public IBizPayAdapter getAdapter(BizPayProvider provider)
    {
        if (provider == null)
        {
            throw new ServiceException("支付服务商不存在");
        }
        if (BizConstants.PAY_MOCK_YES.equals(provider.getMockMode())
                || StringUtils.isEmpty(provider.getGatewayUrl())
                || provider.getGatewayUrl().contains("mock.pay.local"))
        {
            return mockPayAdapter;
        }
        String family = provider.getAdapterFamily() == null ? "" : provider.getAdapterFamily().trim().toLowerCase();
        if (BizConstants.PAY_FAMILY_MONPAY.equals(family) || "feifan".equals(family) || "nicepay".equals(family))
        {
            return monPayAdapter;
        }
        if (BizConstants.PAY_FAMILY_WUYOU.equals(family) || "xxpay".equals(family))
        {
            return wuYouAdapter;
        }
        if (BizConstants.PAY_FAMILY_JEEPAY.equals(family) || "fuwang".equals(family))
        {
            return jeepayAdapter;
        }
        throw new ServiceException("服务商 " + provider.getProviderName() + " 尚未接入真实网关");
    }
}
