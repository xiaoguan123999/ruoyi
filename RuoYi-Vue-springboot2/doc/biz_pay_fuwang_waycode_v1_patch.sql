SET NAMES utf8mb4;
-- FuWang API v1: wayCode must be numeric (doc example 901), not ALI_QR / WX_NATIVE.
-- Incremental patch on top of ry-vue-local-20260920.sql. Safe to re-run.
-- Before production, replace product_id with the real wayCode enabled in merchant console.

UPDATE biz_pay_channel
SET product_id = '901',
    update_time = sysdate()
WHERE channel_code IN ('FUWANG_ALIPAY', 'FUWANG_WECHAT')
  AND IFNULL(product_id, '') <> '901';
