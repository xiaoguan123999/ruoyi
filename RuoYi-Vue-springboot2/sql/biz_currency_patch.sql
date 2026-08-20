-- CNY / USDT independent settlement
-- ALTER may error if column already exists; data statements are safe to re-run.

ALTER TABLE biz_product
  ADD COLUMN currency varchar(16) NOT NULL DEFAULT 'CNY' COMMENT 'CNY/USDT' AFTER product_name;

ALTER TABLE biz_order
  ADD COLUMN currency varchar(16) NOT NULL DEFAULT 'CNY' COMMENT 'CNY/USDT' AFTER product_name;

ALTER TABLE biz_rebate_log
  ADD COLUMN currency varchar(16) NOT NULL DEFAULT 'CNY' COMMENT 'CNY/USDT' AFTER member_id;

UPDATE sys_config
   SET config_value = 'true'
 WHERE config_key = 'biz.usdt.enabled';

DELETE FROM sys_config WHERE config_id = 27 OR config_key = 'biz.withdraw.minAmount.usdt';
INSERT INTO sys_config VALUES (27, 'USDT min withdraw', 'biz.withdraw.minAmount.usdt', '105', 'N', 'admin', sysdate(), '', NULL, 'USDT min withdraw');

INSERT INTO biz_product (product_id, product_name, currency, price, daily_rebate, duration_days, withdraw_required, status, sort, create_by, create_time, remark)
SELECT 2, 'USDT Product', 'USDT', 100.0000, 5.0000, 30, '1', '0', 2, 'admin', sysdate(), 'USDT withdraw required'
WHERE NOT EXISTS (SELECT 1 FROM biz_product WHERE product_id = 2);
