SET NAMES utf8mb4;
-- ============================================================
-- 大转盘配置化抽奖 增量脚本（可重复执行）
-- 合并自：lottery-wheel-patch.sql + lottery-chance-rule-max-repeat.sql
-- 对齐本仓库：biz_* 命名、BaseEntity 审计字段、status 0正常/1停用
-- 含：人群包、活动/奖池/必中、获次规则(仅一次/按倍数不限或限档)、次数余额流水、菜单
-- 用法：目标业务库执行本文件即可。
-- ============================================================

-- ------------------------------------------------------------
-- A. 营销标签人群（资产化，多行条件，禁止 JSON 落库）
-- ------------------------------------------------------------

create table if not exists biz_crowd_package (
  package_id        bigint(20)      not null auto_increment    comment '人群包ID',
  package_name      varchar(64)     not null                   comment '人群包名称',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (package_id),
  unique key uk_biz_crowd_package_name (package_name)
) engine=innodb comment = '营销标签人群包';

create table if not exists biz_crowd_condition (
  condition_id      bigint(20)      not null auto_increment    comment '条件ID',
  package_id        bigint(20)      not null                   comment '所属人群包ID',
  label_type        varchar(32)     not null                   comment '标签维度 IMAGE画像 TRANSACTION资产 ACTION行为',
  label_field       varchar(64)     not null                   comment '标签字段 user_type/charge_amount/invite_count',
  operator_type     varchar(16)     not null                   comment '运算符 EQUALS/GREATER_THAN/LESS_THAN',
  rule_value        varchar(128)    not null                   comment '限制值',
  sort              int(4)          default 0                  comment '排序',
  primary key (condition_id),
  key idx_crowd_condition_package (package_id)
) engine=innodb comment = '营销人群包原子条件';

-- ------------------------------------------------------------
-- B. 抽奖活动 / 奖品双池 / 步进必中策略
-- ------------------------------------------------------------

create table if not exists biz_lottery_activity (
  activity_id       bigint(20)      not null auto_increment    comment '活动ID',
  title             varchar(128)    not null                   comment '活动名称',
  start_time        datetime        default null               comment '开始时间，空=立即生效',
  end_time          datetime        default null               comment '结束时间，空=长期有效',
  interval_hours    int(11)         not null default 72        comment '抽奖频控间隔(小时)',
  rule_text         text            default null               comment 'App展示抽奖规则（多行文本）',
  status            char(1)         default '1'                comment '0正常(启用) 1停用；新建默认停用防误开',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (activity_id),
  key idx_lottery_act_duration (start_time, end_time, status)
) engine=innodb comment = '大转盘抽奖活动';


-- 兼容已建表：App 抽奖规则文案
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_activity' and column_name = 'rule_text');
set @sql := if(@exist = 0, 'alter table biz_lottery_activity add column rule_text text default null comment ''App展示抽奖规则（多行文本）'' after interval_hours', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 兼容已建表：时间可空（长期活动）
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_activity' and column_name = 'start_time' and is_nullable = 'NO');
set @sql := if(@exist > 0, 'alter table biz_lottery_activity modify column start_time datetime default null comment ''开始时间，空=立即生效''', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_activity' and column_name = 'end_time' and is_nullable = 'NO');
set @sql := if(@exist > 0, 'alter table biz_lottery_activity modify column end_time datetime default null comment ''结束时间，空=长期有效''', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_lottery_prize_pool (
  pool_id           bigint(20)      not null auto_increment    comment '奖品池ID',
  prize_name        varchar(64)     not null                   comment '奖品名称',
  prize_desc        varchar(128)    default null               comment '奖品说明',
  image_url         varchar(512)    default null               comment '奖品图片',
  sector_bg_color   varchar(32)     default '#FFF8EC'          comment '转盘扇区背景色',
  name_color        varchar(32)     default '#111827'          comment '名称字体颜色',
  desc_color        varchar(32)     default '#374151'          comment '说明字体颜色',
  prize_type        tinyint(4)      not null                   comment '1实物 2现金 3虚拟资产',
  assist_amount     decimal(18,4)   default null               comment '虚拟助力值额度(prize_type=3)',
  currency          varchar(16)     default 'CNY'              comment '虚拟入账币种',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (pool_id)
) engine=innodb comment = '抽奖奖品池（独立管理）';

create table if not exists biz_lottery_prize (
  prize_id          bigint(20)      not null auto_increment    comment '活动奖项ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  pool_id           bigint(20)      default null               comment '奖品池ID',
  prize_name        varchar(64)     not null                   comment '奖品名称快照',
  prize_desc        varchar(128)    default null               comment '奖品说明快照',
  image_url         varchar(512)    default null               comment '奖品图片快照',
  sector_bg_color   varchar(32)     default null               comment '扇区背景色快照',
  name_color        varchar(32)     default null               comment '名称颜色快照',
  desc_color        varchar(32)     default null               comment '说明颜色快照',
  prize_type        tinyint(4)      not null                   comment '1实物 2现金 3虚拟资产',
  public_stock      int(11)         not null default 0         comment '公海库存 -1无限(仅虚拟允许)',
  exclusive_stock   int(11)         not null default 0         comment '专属库存 -1无限(仅虚拟允许)',
  probability       int(11)         not null default 0         comment '公海中奖概率(万分制)',
  position          tinyint(4)      not null                   comment '转盘排序/扇区序号，从1起可扩展',
  is_fallback       char(1)         default '0'                comment '是否兜底奖 0否 1是',
  assist_amount     decimal(18,4)   default null               comment '虚拟助力值额度(prize_type=3)',
  currency          varchar(16)     default 'CNY'              comment '虚拟入账币种',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (prize_id),
  key idx_lottery_prize_act (activity_id),
  key idx_lottery_prize_pool (pool_id),
  unique key uk_lottery_prize_position (activity_id, position)
) engine=innodb comment = '活动转盘奖项（引用奖品池+双库存）';

-- 兼容已建表：奖品池 + 活动奖项 pool_id
set @exist := (select count(*) from information_schema.tables where table_schema = database() and table_name = 'biz_lottery_prize_pool');
set @sql := if(@exist = 0, 'create table biz_lottery_prize_pool (
  pool_id bigint(20) not null auto_increment comment ''奖品池ID'',
  prize_name varchar(64) not null comment ''奖品名称'',
  prize_type tinyint(4) not null comment ''1实物 2现金 3虚拟资产'',
  assist_amount decimal(18,4) default null comment ''虚拟助力值额度'',
  currency varchar(16) default ''CNY'' comment ''虚拟入账币种'',
  sort int(4) default 0 comment ''排序'',
  status char(1) default ''0'' comment ''0正常 1停用'',
  create_by varchar(64) default '''' comment ''创建者'',
  create_time datetime comment ''创建时间'',
  update_by varchar(64) default '''' comment ''更新者'',
  update_time datetime comment ''更新时间'',
  remark varchar(500) default null comment ''备注'',
  primary key (pool_id)
) engine=innodb comment=''抽奖奖品池''', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'prize_desc');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column prize_desc varchar(128) default null comment ''奖品说明'' after prize_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'image_url');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column image_url varchar(512) default null comment ''奖品图片'' after prize_desc', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'sector_bg_color');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column sector_bg_color varchar(32) default ''#FFF8EC'' comment ''转盘扇区背景色'' after image_url', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'name_color');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column name_color varchar(32) default ''#111827'' comment ''名称字体颜色'' after sector_bg_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize_pool' and column_name = 'desc_color');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize_pool add column desc_color varchar(32) default ''#374151'' comment ''说明字体颜色'' after name_color', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_prize' and column_name = 'pool_id');
set @sql := if(@exist = 0, 'alter table biz_lottery_prize add column pool_id bigint(20) default null comment ''奖品池ID'' after activity_id, add key idx_lottery_prize_pool (pool_id)', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_lottery_win_strategy (
  strategy_id       bigint(20)      not null auto_increment    comment '策略ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  target_type       tinyint(4)      not null                   comment '1全员 2指定用户 3人群包',
  user_ids          text            default null               comment '指定会员ID逗号分隔(target_type=2)',
  package_id        bigint(20)      default null               comment '人群包ID(target_type=3)',
  target_prize_id   bigint(20)      not null                   comment '必中奖品ID',
  trigger_count     int(11)         not null                   comment '第X次抽奖必中',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (strategy_id),
  key idx_lottery_strategy_act (activity_id, trigger_count, status),
  key idx_lottery_strategy_pkg (package_id)
) engine=innodb comment = '大转盘步进必中策略';

-- 兼容已建表：必中策略库存模式
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_win_strategy' and column_name = 'stock_mode');
set @sql := if(@exist = 0, 'alter table biz_lottery_win_strategy add column stock_mode varchar(16) not null default ''AUTO'' comment ''必中扣库存 EXCLUSIVE/PUBLIC/AUTO'' after trigger_count', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;


-- ------------------------------------------------------------
-- C. 用户抽奖概况 + 流水（强幂等）
-- ------------------------------------------------------------

create table if not exists biz_lottery_member (
  id                bigint(20)      not null auto_increment    comment '主键',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  draw_count        int(11)         not null default 0         comment '累计抽奖次数(落库备份)',
  last_draw_time    datetime        default null               comment '最近抽奖时间',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_lottery_member (activity_id, member_id)
) engine=innodb comment = '会员抽奖资格概况';

create table if not exists biz_lottery_record (
  record_id         bigint(20)      not null auto_increment    comment '流水ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  draw_count        int(11)         not null                   comment '本次为第几次',
  prize_id          bigint(20)      default null               comment '奖品ID(熔断时可空)',
  prize_name        varchar(64)     default ''                 comment '奖品名称快照',
  prize_type        tinyint(4)      default null               comment '奖品类型快照',
  pool_type         varchar(16)     default null               comment 'PUBLIC公海 EXCLUSIVE专属',
  is_strategy       char(1)         default '0'                comment '是否步进必中 0否 1是',
  is_meltdown       char(1)         default '0'                comment '是否库存熔断 0否 1是',
  grant_status      char(1)         default '0'                comment '0待处理 1已入账 2待领取 3已关闭',
  biz_no            varchar(64)     default null               comment '幂等业务号',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (record_id),
  unique key uk_lottery_record_draw (activity_id, member_id, draw_count),
  unique key uk_lottery_record_biz (biz_no),
  key idx_lottery_record_member (member_id, create_time),
  key idx_lottery_record_act (activity_id, create_time)
) engine=innodb comment = '大转盘抽奖流水';

-- ------------------------------------------------------------
-- D. 入账规则（虚拟助力值走 ASSIST 钱包）
-- ------------------------------------------------------------

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time, remark)
select 'LOTTERY_ASSIST', '大转盘助力值', 'ASSIST', '1', 30, 'admin', sysdate(), '大转盘虚拟奖入账助力钱包'
from dual where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'LOTTERY_ASSIST');

-- ------------------------------------------------------------
-- E. 后台菜单（顶级「抽奖系统」，与运营中心同级）
-- ------------------------------------------------------------

delete from sys_role_menu where menu_id between 2405 and 2445;
delete from sys_menu where menu_id between 2405 and 2445;

-- 为插入 order_num=5 腾位（可重复执行：仅当 2405 不存在时移位，简化为固定重排业务菜单较危险，此处仅插入顶级）
-- 顶级目录
insert into sys_menu values(2405, '抽奖系统', 0, 5, 'lottery', null, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', sysdate(), '', null, '抽奖系统：人群、奖品、活动、记录');

insert into sys_menu values(2410, '标签人群', 2405, 1, 'lotteryCrowd', 'biz/lottery/crowdPackage/index', '', '', 1, 0, 'C', '0', '0', 'biz:crowdPackage:list', 'peoples', 'admin', sysdate(), '', null, '营销标签人群管理中心，全站复用');
insert into sys_menu values(2411, '人群查询', 2410, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2412, '人群新增', 2410, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2413, '人群修改', 2410, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2414, '人群删除', 2410, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:crowdPackage:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2415, '奖品管理', 2405, 2, 'lotteryPrize', 'biz/lottery/prize/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryPrize:edit', 'shopping', 'admin', sysdate(), '', null, '独立奖品池：名称/类型/虚拟配置');
insert into sys_menu values(2416, '奖品增删改', 2415, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryPrize:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2420, '抽奖活动', 2405, 3, 'lotteryActivity', 'biz/lottery/activity/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryActivity:list', 'guide', 'admin', sysdate(), '', null, '抽奖活动/必中策略配置');
insert into sys_menu values(2421, '活动查询', 2420, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2422, '活动新增', 2420, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2423, '活动修改', 2420, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2424, '活动删除', 2420, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryActivity:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2425, '奖品配置', 2420, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryPrize:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2426, '必中策略', 2420, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryStrategy:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values(2430, '抽奖记录', 2405, 4, 'lotteryRecord', 'biz/lottery/record/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryRecord:list', 'log', 'admin', sysdate(), '', null, '大转盘抽奖流水查询');
insert into sys_menu values(2431, '记录查询', 2430, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryRecord:query', '#', 'admin', sysdate(), '', null, '');

-- 授权：已有运营中心或其子菜单权限的角色 + 超管
insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
cross join sys_menu m
where rm.menu_id in (select menu_id from sys_menu where menu_id = 2024 or parent_id = 2024 or menu_id = 2405 or parent_id = 2405)
  and m.menu_id between 2405 and 2435
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

insert into sys_role_menu (role_id, menu_id)
select 1, m.menu_id from sys_menu m
where m.menu_id between 2405 and 2435
  and not exists (select 1 from sys_role_menu x where x.role_id = 1 and x.menu_id = m.menu_id);


-- ------------------------------------------------------------
-- F. 获次规则 + 次数余额/流水
-- ------------------------------------------------------------

create table if not exists biz_lottery_chance_rule (
  rule_id           bigint(20)      not null auto_increment    comment '规则ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  rule_name         varchar(64)     not null                   comment '规则名称',
  checkin_days      int(11)         not null default 0         comment '需累计签到天数，0=不限制',
  invite_kyc_count  int(11)         not null default 0         comment '需直推实名人数，0=不限制',
  grant_amount      int(11)         not null default 1         comment '每档达标发放次数',
  once_only         char(1)         default '1'                comment '1仅发放一次 0按倍数可重复',
  max_repeat        int(11)         not null default 0         comment '重复最多档次：0=不限按倍数全发；>0=封顶；仅一次时为1',
  status            char(1)         default '0'                comment '0启用 1停用',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (rule_id),
  key idx_lottery_chance_rule_act (activity_id, status)
) engine=innodb comment='抽奖获次规则';

create table if not exists biz_lottery_chance (
  id                bigint(20)      not null auto_increment    comment '主键',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  balance           int(11)         not null default 0         comment '可用抽奖次数',
  total_granted     int(11)         not null default 0         comment '累计发放',
  total_consumed    int(11)         not null default 0         comment '累计消耗',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (id),
  unique key uk_lottery_chance (activity_id, member_id),
  key idx_lottery_chance_member (member_id)
) engine=innodb comment='会员抽奖次数余额';

create table if not exists biz_lottery_chance_log (
  log_id            bigint(20)      not null auto_increment    comment '流水ID',
  activity_id       bigint(20)      not null                   comment '活动ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  change_type       varchar(32)     not null                   comment 'RULE_GRANT/ADMIN_GRANT/ADMIN_DEDUCT/DRAW_CONSUME',
  change_amount     int(11)         not null                   comment '变动值，正增负减',
  balance_after     int(11)         not null                   comment '变动后余额',
  biz_no            varchar(64)     not null                   comment '幂等业务号',
  rule_id           bigint(20)      default null               comment '关联获次规则',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '操作者',
  create_time       datetime                                   comment '创建时间',
  primary key (log_id),
  unique key uk_lottery_chance_biz (biz_no),
  key idx_lottery_chance_log_m (member_id, create_time),
  key idx_lottery_chance_log_a (activity_id, create_time)
) engine=innodb comment='抽奖次数变动流水';

insert into sys_menu values(2432, '获次规则', 2405, 5, 'lotteryChanceRule', 'biz/lottery/chanceRule/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryChanceRule:list', 'edit', 'admin', sysdate(), '', null, '抽奖次数获得规则');
insert into sys_menu values(2433, '规则查询', 2432, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2434, '规则新增', 2432, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2435, '规则修改', 2432, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2436, '规则删除', 2432, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChanceRule:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2440, '用户次数', 2405, 6, 'lotteryChance', 'biz/lottery/chance/index', '', '', 1, 0, 'C', '0', '0', 'biz:lotteryChance:list', 'number', 'admin', sysdate(), '', null, '用户抽奖次数与流水');
insert into sys_menu values(2441, '次数查询', 2440, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChance:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values(2442, '次数调整', 2440, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'biz:lotteryChance:adjust', '#', 'admin', sysdate(), '', null, '');

-- 必中触发模式：ONCE仅第N次 / LOOP每N次循环
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_win_strategy' and column_name = 'loop_mode');
set @sql := if(@exist = 0, 'alter table biz_lottery_win_strategy add column loop_mode varchar(16) not null default ''ONCE'' comment ''触发模式 ONCE仅一次 LOOP每N次循环'' after trigger_count', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 奖品说明加长并支持多行
alter table biz_lottery_prize_pool modify column prize_desc varchar(500) default null comment '奖品说明，支持换行';
alter table biz_lottery_prize modify column prize_desc varchar(500) default null comment '奖品说明快照，支持换行';

-- 获次规则兼容：max_repeat（0=按倍数不限档；>0=最多 N 档；once_only=1 仅一次）
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_lottery_chance_rule' and column_name = 'max_repeat');
set @sql := if(@exist = 0, 'alter table biz_lottery_chance_rule add column max_repeat int(11) not null default 0 comment ''重复最多档次：0=不限按倍数全发；>0=封顶；仅一次时为1'' after once_only', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;
alter table biz_lottery_chance_rule modify column once_only char(1) default '1' comment '1仅发放一次 0按倍数可重复';
