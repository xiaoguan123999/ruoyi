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
        throw new ServiceException("服务商 " + provider.getProviderName() + " 尚未接入真实网关，请保持模拟模式");
    }
}
