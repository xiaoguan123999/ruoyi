SET NAMES utf8mb4;
-- 团队路径前缀索引 + 登录日志查询索引。可重复执行。

set @exist := (
  select count(*) from information_schema.statistics
  where table_schema = database()
    and table_name = 'biz_member'
    and index_name = 'idx_biz_member_ancestors'
);
set @sql := if(@exist = 0,
  'alter table biz_member add index idx_biz_member_ancestors (ancestors)',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.statistics
  where table_schema = database()
    and table_name = 'biz_member_logininfor'
    and index_name = 'idx_biz_member_login_lookup'
);
set @sql := if(@exist = 0,
  'alter table biz_member_logininfor add index idx_biz_member_login_lookup (member_id, status, msg, login_time)',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;
