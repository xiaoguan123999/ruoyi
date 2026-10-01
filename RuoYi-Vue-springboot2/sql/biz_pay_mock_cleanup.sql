SET NAMES utf8mb4;

-- Cleanup mock pay seed: baifu / baoli / niupay / shapay / baoli_u
-- Screenshot 9 channels + BAIFU_UNION (same seed).
-- Does NOT touch fuwang / wuyou / feifan.
-- Run SELECT first, then DELETE.

SELECT provider_code, provider_name, mock_mode, gateway_url, app_id, status
FROM biz_pay_provider
WHERE provider_code IN ('baifu', 'baoli', 'niupay', 'shapay', 'baoli_u');

SELECT c.channel_code, c.display_name, c.provider_code, c.scene, c.product_id, c.status, p.mock_mode
FROM biz_pay_channel c
LEFT JOIN biz_pay_provider p ON p.provider_code = c.provider_code
WHERE c.provider_code IN ('baifu', 'baoli', 'niupay', 'shapay', 'baoli_u')
   OR c.channel_code IN (
        'BAIFU_ALIPAY','BAIFU_WECHAT','BAIFU_UNION',
        'BAOLI_ALIPAY','BAOLI_WECHAT',
        'NIUPAY_ALIPAY','NIUPAY_WECHAT',
        'SHAPAY_ALIPAY','SHAPAY_WECHAT',
        'BAOLI_U_DEPOSIT'
   );

SELECT COUNT(*) AS mock_pay_order_cnt
FROM biz_pay_order
WHERE provider_code IN ('baifu', 'baoli', 'niupay', 'shapay', 'baoli_u');

-- Order: gateway log -> pay order -> channel -> provider
-- No FK. Wallet credits from already-paid recharge are kept.
-- To keep mock pay-order history, comment out the two DELETE below.

DELETE FROM biz_pay_gateway_log
WHERE provider_code IN ('baifu', 'baoli', 'niupay', 'shapay', 'baoli_u');

DELETE FROM biz_pay_order
WHERE provider_code IN ('baifu', 'baoli', 'niupay', 'shapay', 'baoli_u');

DELETE FROM biz_pay_channel
WHERE provider_code IN ('baifu', 'baoli', 'niupay', 'shapay', 'baoli_u')
   OR channel_code IN (
        'BAIFU_ALIPAY','BAIFU_WECHAT','BAIFU_UNION',
        'BAOLI_ALIPAY','BAOLI_WECHAT',
        'NIUPAY_ALIPAY','NIUPAY_WECHAT',
        'SHAPAY_ALIPAY','SHAPAY_WECHAT',
        'BAOLI_U_DEPOSIT'
   );

DELETE FROM biz_pay_provider
WHERE provider_code IN ('baifu', 'baoli', 'niupay', 'shapay', 'baoli_u');
