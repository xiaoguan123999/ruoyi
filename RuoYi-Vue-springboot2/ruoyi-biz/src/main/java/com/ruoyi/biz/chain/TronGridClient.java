package com.ruoyi.biz.chain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * TronGrid confirmed USDT-TRC20 transfers to a shared collection address.
 */
@Component
public class TronGridClient
{
    private static final Logger log = LoggerFactory.getLogger(TronGridClient.class);
    private static final String BASE = "https://api.trongrid.io";
    private static final int PAGE_LIMIT = 200;
    private static final int MAX_PAGES = 5;

    public List<TronUsdtTransfer> listIncomingUsdt(String address, String apiKey, Date since)
    {
        List<TronUsdtTransfer> out = new ArrayList<TronUsdtTransfer>();
        if (StringUtils.isEmpty(address))
        {
            return out;
        }
        Map<String, String> headers = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(apiKey))
        {
            headers.put("TRON-PRO-API-KEY", apiKey);
        }
        long minTs = since == null ? System.currentTimeMillis() - 40L * 60L * 1000L : since.getTime() - 5L * 60L * 1000L;
        String fingerprint = null;
        for (int page = 0; page < MAX_PAGES; page++)
        {
            StringBuilder url = new StringBuilder();
            url.append(BASE).append("/v1/accounts/").append(address.trim())
                    .append("/transactions/trc20?only_to=true&only_confirmed=true&limit=")
                    .append(PAGE_LIMIT)
                    .append("&contract_address=").append(BizConstants.USDT_TRC20_CONTRACT)
                    .append("&min_timestamp=").append(minTs);
            if (StringUtils.isNotEmpty(fingerprint))
            {
                url.append("&fingerprint=").append(fingerprint);
            }
            String raw = PayHttp.get(url.toString(), headers);
            JSONObject json = JSON.parseObject(raw);
            if (json == null)
            {
                break;
            }
            JSONArray data = json.getJSONArray("data");
            if (data == null || data.isEmpty())
            {
                break;
            }
            for (int i = 0; i < data.size(); i++)
            {
                TronUsdtTransfer row = parse(data.getJSONObject(i), address);
                if (row != null)
                {
                    out.add(row);
                }
            }
            JSONObject meta = json.getJSONObject("meta");
            fingerprint = meta == null ? null : meta.getString("fingerprint");
            if (StringUtils.isEmpty(fingerprint))
            {
                break;
            }
        }
        log.info("trongrid incoming usdt address={} size={}", mask(address), Integer.valueOf(out.size()));
        return out;
    }

    private static TronUsdtTransfer parse(JSONObject item, String collection)
    {
        if (item == null)
        {
            return null;
        }
        String to = item.getString("to");
        if (StringUtils.isEmpty(to) || !to.equalsIgnoreCase(collection.trim()))
        {
            return null;
        }
        String from = item.getString("from");
        if (StringUtils.isNotEmpty(from) && from.equalsIgnoreCase(collection.trim()))
        {
            return null;
        }
        String hash = item.getString("transaction_id");
        if (StringUtils.isEmpty(hash))
        {
            return null;
        }
        String value = item.getString("value");
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        int decimals = 6;
        JSONObject token = item.getJSONObject("token_info");
        if (token != null && token.getInteger("decimals") != null)
        {
            decimals = token.getInteger("decimals").intValue();
        }
        BigDecimal amount;
        try
        {
            amount = new BigDecimal(value).movePointLeft(decimals).setScale(6, java.math.RoundingMode.DOWN);
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
        row.setNetwork(BizConstants.CHAIN_NETWORK_TRC20);
        row.setTxHash(hash);
        row.setFromAddress(from);
        row.setToAddress(to);
        row.setAmount(amount);
        Long ts = item.getLong("block_timestamp");
        if (ts != null && ts.longValue() > 0L)
        {
            row.setBlockTime(new Date(ts.longValue()));
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
