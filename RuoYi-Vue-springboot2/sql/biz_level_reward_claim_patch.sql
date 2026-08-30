SET NAMES utf8mb4;
-- 等级奖励：用户领取 + 二选一/都可领。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_level'
    and column_name = 'reward_claim_policy'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column reward_claim_policy varchar(16) not null default ''ONE'' comment ''CLAIM时 ONE二选一 ALL都可领取'' after reward_mode',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;
