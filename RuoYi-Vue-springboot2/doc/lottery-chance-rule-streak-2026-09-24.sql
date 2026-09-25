SET NAMES utf8mb4;
-- 获次规则增加连续签到天数：0=不限制该项
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_chance_rule' and column_name = 'streak_days');
set @sql := if(@exist = 0, 'alter table biz_lottery_chance_rule add column streak_days int(11) not null default 0 comment ''连续签到天数，0=不限制'' after checkin_days', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
