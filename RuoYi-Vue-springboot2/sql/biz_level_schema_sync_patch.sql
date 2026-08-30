SET NAMES utf8mb4;
-- 补齐 biz_level 缺列，避免 /biz/level/list 报 Unknown column 'performance_source'
-- 可重复执行；在业务库（如 ry-vue）跑一遍即可，然后重启 Java

-- team_depth
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'team_depth');
set @sql := if(@exist = 0, 'alter table biz_level add column team_depth varchar(50) default '''' comment ''team depth'' after min_valid_members', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- performance_source
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'performance_source');
set @sql := if(@exist = 0, 'alter table biz_level add column performance_source varchar(16) not null default ''SUBSCRIBE'' comment ''SUBSCRIBE/RECHARGE/BOTH'' after team_depth', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_recharge_cny
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_recharge_cny decimal(18,4) not null default 0 comment ''team recharge CNY'' after min_recharge_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_recharge_usdt
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_recharge_usdt decimal(18,4) not null default 0 comment ''team recharge USDT'' after min_team_recharge_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_perf_cny
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_cny decimal(18,4) default 0 comment ''team perf CNY'' after min_team_recharge_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_perf_usdt
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_usdt decimal(18,4) default 0 comment ''team perf USDT'' after min_team_perf_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- reward_*
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_enabled');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_enabled char(1) default ''0'' comment ''reward enabled'' after remark', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cycle');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cycle varchar(20) default ''NONE'' comment ''reward cycle'' after reward_enabled', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_mode');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_mode varchar(20) default ''AUTO'' comment ''reward mode'' after reward_cycle', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_repeat');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_repeat varchar(20) default ''NONE'' comment ''reward repeat'' after reward_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cny decimal(18,4) default 0 comment ''reward CNY'' after reward_repeat', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_usdt decimal(18,4) default 0 comment ''reward USDT'' after reward_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- wallet / valid member rules
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'wallet_type_code');
set @sql := if(@exist = 0, 'alter table biz_level add column wallet_type_code varchar(32) not null default ''PROMO'' comment ''wallet type'' after reward_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'mixed_pay_currency');
set @sql := if(@exist = 0, 'alter table biz_level add column mixed_pay_currency varchar(16) not null default ''USDT'' comment ''USDT/CNY/BOTH'' after wallet_type_code', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_kyc');
set @sql := if(@exist = 0, 'alter table biz_level add column valid_need_kyc char(1) not null default ''1'' comment ''need kyc'' after mixed_pay_currency', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_order');
set @sql := if(@exist = 0, 'alter table biz_level add column valid_need_order char(1) not null default ''1'' comment ''need order'' after valid_need_kyc', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_level_reward_grant (
  grant_id          bigint(20)      not null auto_increment    comment 'grant id',
  member_id         bigint(20)      not null                   comment 'member id',
  level_id          bigint(20)      not null                   comment 'level id',
  level_name        varchar(50)     default ''                 comment 'level name',
  cycle_key         varchar(40)     not null                   comment 'cycle key',
  grant_cycle       varchar(20)     default ''                 comment 'ONCE MONTHLY PERMANENT',
  grant_mode        varchar(20)     default ''                 comment 'AUTO MANUAL',
  currency          varchar(10)     default 'CNY'              comment 'currency',
  amount            decimal(18,4)   default 0                  comment 'amount',
  status            char(1)         default '0'                comment '0 pending 1 paid 2 reject',
  pay_by            varchar(64)     default ''                 comment 'payer',
  pay_time          datetime                                   comment 'pay time',
  create_time       datetime                                   comment 'create time',
  remark            varchar(500)    default null               comment 'remark',
  primary key (grant_id),
  unique key uk_level_reward_cycle (member_id, level_id, cycle_key),
  key idx_level_reward_member (member_id),
  key idx_level_reward_status (status)
) engine=innodb comment = 'level reward grant';

update biz_level set team_depth = '一级内' where level_name = '启航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '二级内' where level_name = '探索' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '三级内' where level_name = '开拓' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '四级内' where level_name = '星耀' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '五级内' where level_name = '领航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '六级内' where level_name = '星域' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '七级内' where level_name = '星链' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '' where team_depth is null;
