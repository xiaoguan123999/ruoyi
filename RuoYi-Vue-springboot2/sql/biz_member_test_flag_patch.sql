SET NAMES utf8mb4;
-- 会员测试标记。打标后其数据不计入任何统计。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_member'
    and column_name = 'test_flag'
);
set @sql := if(@exist = 0,
  'alter table biz_member add column test_flag char(1) not null default ''0'' comment ''测试账号 0否 1是'' after status',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_member set test_flag = '0' where test_flag is null;
