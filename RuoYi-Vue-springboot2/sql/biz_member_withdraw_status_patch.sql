SET NAMES utf8mb4;
-- 会员提现状态。0正常 1禁止。默认正常。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_member'
    and column_name = 'withdraw_status'
);
set @sql := if(@exist = 0,
  'alter table biz_member add column withdraw_status char(1) not null default ''0'' comment ''提现状态 0正常 1禁止'' after status',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_member set withdraw_status = '0' where withdraw_status is null;
