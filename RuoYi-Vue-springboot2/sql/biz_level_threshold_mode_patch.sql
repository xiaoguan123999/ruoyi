SET NAMES utf8mb4;
-- 等级门槛：分币种 SPLIT（默认，与现网一致） / 折合人民币 EQUIV
-- 汇率写在 sys_config.biz.fx.usdtToCny，默认 6.25（1 USDT = 6.25 CNY）
-- 可重复执行；跑完需重启 Java

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_level'
    and column_name = 'threshold_mode'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column threshold_mode varchar(16) not null default ''SPLIT'' comment ''SPLIT分币种同时达标 EQUIV折合人民币合计达标'' after performance_source',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'USDT to CNY rate', 'biz.fx.usdtToCny', '6.25', 'N', 'admin', sysdate(), '1 USDT = this many CNY; used by EQUIV level threshold'
from dual
where not exists (select 1 from sys_config where config_key = 'biz.fx.usdtToCny');
