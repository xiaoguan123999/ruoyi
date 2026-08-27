SET NAMES utf8mb4;
-- 团队业绩口径下放到每个等级；可重复执行
-- 本人累计仍按充值；团队累计按该等级口径（认购/充值/认购+充值）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'performance_source'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column performance_source varchar(16) not null default ''SUBSCRIBE'' comment ''团队业绩口径 SUBSCRIBE认购 RECHARGE充值 BOTH认购+充值'' after team_depth',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @src := (
  select config_value from sys_config
  where config_key = 'biz.levelReward.performanceSource'
  limit 1
);
-- 还是建列默认值的等级，沿用原来的全局口径；已经改过的不覆盖
update biz_level
set performance_source = ifnull(@src, 'SUBSCRIBE')
where performance_source = 'SUBSCRIBE';

-- 把对话框里看不到的「团队业绩」数字挪到「团队累计」，避免两套门槛
update biz_level
set min_team_recharge_cny = min_team_perf_cny,
    min_team_recharge_usdt = min_team_perf_usdt
where ifnull(min_team_recharge_cny, 0) = 0
  and ifnull(min_team_recharge_usdt, 0) = 0
  and (ifnull(min_team_perf_cny, 0) > 0 or ifnull(min_team_perf_usdt, 0) > 0);

update biz_level
set min_team_perf_cny = 0, min_team_perf_usdt = 0
where ifnull(min_team_perf_cny, 0) > 0 or ifnull(min_team_perf_usdt, 0) > 0;
