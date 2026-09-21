SET NAMES utf8mb4;
-- Team-leader USDT-TRC20 address. Re-runnable.

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_member'
    and column_name = 'chain_address'
);
set @sql := if(@exist = 0,
  'alter table biz_member add column chain_address varchar(128) default '''' comment ''USDT-TRC20 collect address for downline'' after remark',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_chain_deposit'
    and column_name = 'address_source'
);
set @sql := if(@exist = 0,
  'alter table biz_chain_deposit add column address_source varchar(16) not null default ''SYSTEM'' comment ''SYSTEM/TEAM'' after address',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_chain_deposit'
    and column_name = 'collect_member_id'
);
set @sql := if(@exist = 0,
  'alter table biz_chain_deposit add column collect_member_id bigint(20) default null comment ''team leader who owns address'' after address_source',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;
