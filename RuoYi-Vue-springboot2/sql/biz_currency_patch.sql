SET NAMES utf8mb4;
-- CNY / USDT independent settlement（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'currency');
set @sql := if(@exist = 0, 'alter table biz_product add column currency varchar(16) not null default ''CNY'' comment ''CNY/USDT'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'currency');
set @sql := if(@exist = 0, 'alter table biz_order add column currency varchar(16) not null default ''CNY'' comment ''CNY/USDT'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_rebate_log' and column_name = 'currency');
set @sql := if(@exist = 0, 'alter table biz_rebate_log add column currency varchar(16) not null default ''CNY'' comment ''CNY/USDT'' after member_id', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

UPDATE sys_config
   SET config_value = 'true'
 WHERE config_key = 'biz.usdt.enabled';

DELETE FROM sys_config WHERE config_id = 27 OR config_key = 'biz.withdraw.minAmount.usdt';
INSERT INTO sys_config VALUES (27, 'USDT min withdraw', 'biz.withdraw.minAmount.usdt', '105', 'N', 'admin', sysdate(), '', NULL, 'USDT min withdraw');

INSERT INTO biz_product (product_id, product_name, currency, price, daily_rebate, duration_days, withdraw_required, status, sort, create_by, create_time, remark)
SELECT 2, 'USDT Product', 'USDT', 100.0000, 5.0000, 30, '1', '0', 2, 'admin', sysdate(), 'USDT withdraw required'
WHERE NOT EXISTS (SELECT 1 FROM biz_product WHERE product_id = 2);
