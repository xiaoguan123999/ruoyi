package com.ruoyi.biz.chain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.pay.PayHttp;
import com.ruoyi.common.utils.StringUtils;

/**
 * BscScan / Etherscan v2 confirmed USDT-BEP20 transfers to a shared collection address.
 */
@Component
public class BscScanClient
{
    private static final Logger log = LoggerFactory.getLogger(BscScanClient.class);
    private static final String DEFAULT_API = "https://api.bscscan.com/api";
    private static final int PAGE_LIMIT = 100;
    private static final int MAX_PAGES = 3;

    public List<TronUsdtTransfer> listIncomingUsdt(String address, String apiKey, String apiUrl, Date since)
    {
        List<TronUsdtTransfer> out = new ArrayList<TronUsdtTransfer>();
        if (StringUtils.isEmpty(address))
        {
            return out;
        }
        long minTs = since == null ? System.currentTimeMillis() - 40L * 60L * 1000L : since.getTime() - 5L * 60L * 1000L;
        String collection = Bep20Address.normalize(address);
        for (int page = 1; page <= MAX_PAGES; page++)
        {
            String url = buildUrl(collection, apiKey, apiUrl, page);
            String raw = PayHttp.get(url, null);
            JSONObject json = JSON.parseObject(raw);
            if (json == null)
            {
                break;
            }
            String status = json.getString("status");
            Object resultObj = json.get("result");
            if (!"1".equals(status))
            {
                String message = json.getString("message");
                if (resultObj instanceof String && String.valueOf(resultObj).toLowerCase().contains("no transaction"))
                {
                    break;
                }
                if ("No transactions found".equalsIgnoreCase(message))
                {
                    break;
                }
                log.warn("bscscan tokentx fail address={} status={} msg={}", mask(collection), status, message);
                break;
            }
            JSONArray data = json.getJSONArray("result");
            if (data == null || data.isEmpty())
            {
                break;
            }
            boolean older = false;
            for (int i = 0; i < data.size(); i++)
            {
                TronUsdtTransfer row = parse(data.getJSONObject(i), collection, minTs);
                if (row == null)
                {
                    JSONObject item = data.getJSONObject(i);
                    Long ts = item == null ? null : item.getLong("timeStamp");
                    if (ts != null && ts.longValue() * 1000L < minTs)
                    {
                        older = true;
                    }
                    continue;
                }
                out.add(row);
            }
            if (data.size() < PAGE_LIMIT || older)
            {
                break;
            }
        }
        log.info("bscscan incoming usdt address={} size={}", mask(collection), Integer.valueOf(out.size()));
        return out;
    }

    private static String buildUrl(String address, String apiKey, String apiUrl, int page)
    {
        String base = StringUtils.isEmpty(apiUrl) ? DEFAULT_API : apiUrl.trim();
        boolean v2 = base.contains("/v2/api");
        StringBuilder url = new StringBuilder();
        url.append(base);
        url.append(base.indexOf('?') >= 0 ? '&' : '?');
        if (v2 && base.indexOf("chainid=") < 0)
        {
            url.append("chainid=56&");
        }
        url.append("module=account&action=tokentx&contractaddress=")
                .append(BizConstants.USDT_BEP20_CONTRACT)
                .append("&address=").append(address)
                .append("&page=").append(page)
                .append("&offset=").append(PAGE_LIMIT)
                .append("&sort=desc");
        if (StringUtils.isNotEmpty(apiKey))
        {
            url.append("&apikey=").append(apiKey.trim());
        }
        return url.toString();
    }

    private static TronUsdtTransfer parse(JSONObject item, String collection, long minTs)
    {
        if (item == null)
        {
            return null;
        }
        String contract = item.getString("contractAddress");
        if (StringUtils.isEmpty(contract) || !contract.equalsIgnoreCase(BizConstants.USDT_BEP20_CONTRACT))
        {
            return null;
        }
        String to = item.getString("to");
        if (StringUtils.isEmpty(to) || !to.equalsIgnoreCase(collection))
        {
            return null;
        }
        String from = item.getString("from");
        if (StringUtils.isNotEmpty(from) && from.equalsIgnoreCase(collection))
        {
            return null;
        }
        String hash = item.getString("hash");
        if (StringUtils.isEmpty(hash))
        {
            return null;
        }
        Long ts = item.getLong("timeStamp");
        if (ts != null && ts.longValue() * 1000L < minTs)
        {
            return null;
        }
        String value = item.getString("value");
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        int decimals = 18;
        String tokenDecimal = item.getString("tokenDecimal");
        if (StringUtils.isNotEmpty(tokenDecimal))
        {
            try
            {
                decimals = Integer.parseInt(tokenDecimal.trim());
            }
            catch (Exception e)
            {
                decimals = 18;
            }
        }
        BigDecimal amount;
        try
        {
            amount = new BigDecimal(value).movePointLeft(decimals).setScale(6, RoundingMode.DOWN);
        }
        catch (Exception e)
        {
            return null;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return null;
        }
        TronUsdtTransfer row = new TronUsdtTransfer();
        row.setNetwork(BizConstants.CHAIN_NETWORK_BEP20);
        row.setTxHash(hash);
        row.setFromAddress(from == null ? "" : from.toLowerCase());
        row.setToAddress(to.toLowerCase());
        row.setAmount(amount);
        if (ts != null && ts.longValue() > 0L)
        {
            row.setBlockTime(new Date(ts.longValue() * 1000L));
        }
        return row;
    }

    private static String mask(String address)
    {
        if (address == null || address.length() < 8)
        {
            return "";
        }
        return address.substring(0, 4) + "****" + address.substring(address.length() - 4);
    }
}
