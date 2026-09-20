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
            throw new ServiceException("\u652f\u4ed8\u670d\u52a1\u5546\u4e0d\u5b58\u5728");
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
        throw new ServiceException("\u670d\u52a1\u5546 " + provider.getProviderName() + " \u5c1a\u672a\u63a5\u5165\u771f\u5b9e\u7f51\u5173");
    }
}
