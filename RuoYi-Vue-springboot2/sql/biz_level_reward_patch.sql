SET NAMES utf8mb4;
-- 星链伙伴成长激励金：等级奖励配置与发放（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_enabled');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_enabled char(1) default ''0'' comment ''是否启用该等级奖励（0否 1是）'' after remark', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cycle');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cycle varchar(20) default ''NONE'' comment ''ONCE一次 MONTHLY每月 PERMANENT永久 NONE无'' after reward_enabled', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_mode');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_mode varchar(20) default ''AUTO'' comment ''AUTO自动入账 MANUAL客服发放'' after reward_cycle', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_repeat');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_repeat varchar(20) default ''NONE'' comment ''永久档领取：NONE MONTHLY UNLIMITED'' after reward_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cny decimal(18,4) default 0 comment ''奖励金额CNY'' after reward_repeat', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_usdt decimal(18,4) default 0 comment ''奖励金额USDT'' after reward_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_cny decimal(18,4) default 0 comment ''最低团队业绩CNY，0表示不限制'' after min_recharge_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_usdt decimal(18,4) default 0 comment ''最低团队业绩USDT，0表示不限制'' after min_team_perf_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_level_reward_grant (
  grant_id          bigint(20)      not null auto_increment    comment '发放ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  level_id          bigint(20)      not null                   comment '等级ID',
  level_name        varchar(50)     default ''                 comment '等级名称快照',
  cycle_key         varchar(40)     not null                   comment '去重键 ONCE/yyyy-MM/ELIGIBLE/PAY-xxx',
  grant_cycle       varchar(20)     default ''                 comment 'ONCE MONTHLY PERMANENT',
  grant_mode        varchar(20)     default ''                 comment 'AUTO MANUAL',
  currency          varchar(10)     default 'CNY'              comment '发放币种',
  amount            decimal(18,4)   default 0                  comment '发放金额',
  status            char(1)         default '0'                comment '0待发放 1已发放 2已拒绝',
  pay_by            varchar(64)     default ''                 comment '发放人',
  pay_time          datetime                                   comment '发放时间',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (grant_id),
  unique key uk_level_reward_cycle (member_id, level_id, cycle_key),
  key idx_level_reward_member (member_id),
  key idx_level_reward_status (status)
) engine=innodb comment = '等级奖励发放记录';

delete from sys_config where config_id between 41 and 47;
insert into sys_config values(41, '等级奖励开关', 'biz.levelReward.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭成长激励金');
insert into sys_config values(42, '混合业绩发放币种', 'biz.levelReward.mixedPayCurrency', 'USDT', 'N', 'admin', sysdate(), '', null, '团队同时有人民币和USDT业绩时发这个币种');
insert into sys_config values(43, '团队业绩口径', 'biz.levelReward.performanceSource', 'SUBSCRIBE', 'N', 'admin', sysdate(), '', null, 'SUBSCRIBE认购 RECHARGE充值 BOTH两者相加');
insert into sys_config values(44, '团队业绩含本人', 'biz.levelReward.includeSelf', 'false', 'N', 'admin', sysdate(), '', null, 'true表示本人业绩计入团队');
insert into sys_config values(45, '有效成员需实名', 'biz.levelReward.validNeedKyc', 'true', 'N', 'admin', sysdate(), '', null, '有效成员是否必须已实名');
insert into sys_config values(46, '有效成员需认购', 'biz.levelReward.validNeedOrder', 'true', 'N', 'admin', sysdate(), '', null, '有效成员是否必须有认购订单');
insert into sys_config values(47, '等级奖励规则说明', 'biz.levelReward.ruleText', '启航、探索、开拓、星耀、领航、星域：达成条件后系统自动发放1次成长激励金。星链：达成条件后联系客服领取，由后台手动发放。团队同时有人民币和USDT业绩时发放USDT。最终以系统核算为准。', 'N', 'admin', sysdate(), '', null, '展示给App/后台的规则文案');

-- 预置七档名称（停用，避免未配金额就升级）。后台改条件、金额后打开即可。
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '启航', 1, 0, 0, 0, 0, 10, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '启航');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '探索', 3, 0, 0, 0, 0, 20, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '探索');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '开拓', 5, 0, 0, 0, 0, 30, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '开拓');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '星耀', 10, 0, 0, 0, 0, 40, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '星耀');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '领航', 20, 0, 0, 0, 0, 50, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '领航');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '星域', 50, 0, 0, 0, 0, 60, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '星域');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '星链', 100, 0, 0, 0, 0, 70, '1', 'admin', sysdate(), '成长激励金：达标后联系客服，后台手动发放', '1', 'PERMANENT', 'MANUAL', 'UNLIMITED', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '星链');

delete from sys_menu where menu_id in (2019, 2020, 2261, 2262, 2271, 2272, 2273);
insert into sys_menu values('2019', '等级奖励', '2000', '9', 'levelReward', 'biz/levelReward/index', '', '', 1, 0, 'C', '0', '0', 'biz:levelReward:list', 'money', 'admin', sysdate(), '', null, '成长激励金规则与各等级奖励');
insert into sys_menu values('2020', '等级奖励发放', '2000', '9', 'levelRewardGrant', 'biz/levelReward/grant', '', '', 1, 0, 'C', '0', '0', 'biz:levelReward:grant', 'list', 'admin', sysdate(), '', null, '客服确认发放成长激励金');
insert into sys_menu values('2261', '奖励查询', '2019', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2262', '奖励修改', '2019', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2271', '发放查询', '2020', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:grant', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2272', '确认发放', '2020', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:pay', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2273', '拒绝发放', '2020', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:reject', '#', 'admin', sysdate(), '', null, '');

delete from sys_job where job_id = 101;
insert into sys_job values(101, '等级奖励核算', 'DEFAULT', 'levelRewardTask.execute()', '0 15 0 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '每日核算成长激励金：前六档一次自动，星链生成待发放');
