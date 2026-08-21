-- ----------------------------
-- 简易认购返利业务初始化
-- ----------------------------

-- 会员
drop table if exists biz_member;
create table biz_member (
  member_id         bigint(20)      not null auto_increment    comment '会员ID/邀请码',
  phone             varchar(20)     not null                   comment '手机号',
  password          varchar(100)    not null                   comment '密码',
  invite_code       varchar(32)     default ''                 comment '邀请码(7位随机数字)',
  parent_id         bigint(20)      default null               comment '上级会员ID',
  ancestors         varchar(500)    default '0'                comment '祖级列表',
  real_name         varchar(50)     default ''                 comment '真实姓名',
  id_card           varchar(32)     default ''                 comment '身份证号',
  kyc_status        char(1)         default '0'                comment '实名状态（0未实名 1已实名）',
  level_id          bigint(20)      default 1                  comment '会员等级ID',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  ga_secret         varchar(64)     default ''                 comment '谷歌验证密钥',
  ga_status         char(1)         default '0'                comment '谷歌验证（0未绑定 1已绑定）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (member_id),
  unique key uk_biz_member_phone (phone),
  unique key uk_biz_member_invite (invite_code),
  key idx_biz_member_parent (parent_id)
) engine=innodb auto_increment=10001 comment = 'C端会员';

-- 钱包（CNY / USDT 独立）
drop table if exists biz_wallet;
create table biz_wallet (
  wallet_id         bigint(20)      not null auto_increment    comment '钱包ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种 CNY/USDT',
  available         decimal(18,4)   default 0                  comment '可用余额',
  frozen            decimal(18,4)   default 0                  comment '冻结余额',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (wallet_id),
  unique key uk_biz_wallet_member_currency (member_id, currency)
) engine=innodb comment = '会员钱包';

-- 资金流水
drop table if exists biz_wallet_log;
create table biz_wallet_log (
  log_id            bigint(20)      not null auto_increment    comment '流水ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种',
  biz_type          varchar(32)     not null                   comment '业务类型',
  biz_id            bigint(20)      default null               comment '业务单号',
  amount            decimal(18,4)   not null                   comment '变动金额',
  available_before  decimal(18,4)   default 0                  comment '变动前可用',
  available_after   decimal(18,4)   default 0                  comment '变动后可用',
  frozen_before     decimal(18,4)   default 0                  comment '变动前冻结',
  frozen_after      decimal(18,4)   default 0                  comment '变动后冻结',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '创建时间',
  primary key (log_id),
  key idx_biz_wallet_log_member (member_id, create_time)
) engine=innodb comment = '资金流水';

-- 签到
drop table if exists biz_checkin;
create table biz_checkin (
  checkin_id        bigint(20)      not null auto_increment    comment '签到ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  checkin_date      date            not null                   comment '签到日期',
  amount            decimal(18,4)   not null                   comment '奖励金额',
  currency          varchar(16)     default 'CNY'              comment '币种',
  create_time       datetime                                   comment '创建时间',
  primary key (checkin_id),
  unique key uk_biz_checkin_member_date (member_id, checkin_date)
) engine=innodb comment = '每日签到';

-- 签到连续抽奖
drop table if exists biz_checkin_prize;
create table biz_checkin_prize (
  prize_log_id      bigint(20)      not null auto_increment    comment '抽奖记录ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  checkin_id        bigint(20)      not null                   comment '签到ID',
  streak_days       int(11)         not null                   comment '连续签到天数',
  prize_name        varchar(100)    not null                   comment '奖品名称',
  won               char(1)         not null default '0'       comment '是否中奖（0未中 1已中）',
  create_time       datetime                                   comment '创建时间',
  primary key (prize_log_id),
  unique key uk_biz_checkin_prize_once (member_id, checkin_id, streak_days),
  key idx_biz_checkin_prize_member (member_id, won)
) engine=innodb comment = '签到连续抽奖记录';



-- 产品
drop table if exists biz_product;
create table biz_product (
  product_id        bigint(20)      not null auto_increment    comment '产品ID',
  product_name      varchar(100)    not null                   comment '产品名称',
  currency          varchar(16)     not null default 'CNY'     comment 'CNY/USDT',
  price             decimal(18,4)   not null                   comment '认购价格(CNY)',
  daily_rebate      decimal(18,4)   not null                   comment '每日返利(CNY)',
  duration_days     int(11)         not null                   comment '返利天数',
  withdraw_required char(1)         default '0'                comment '是否提现指定产品（0否 1是）',
  status            char(1)         default '0'                comment '状态（0上架 1下架）',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (product_id)
) engine=innodb auto_increment=1 comment = '认购产品';

-- 认购订单
drop table if exists biz_order;
create table biz_order (
  order_id          bigint(20)      not null auto_increment    comment '订单ID',
  order_no          varchar(32)     not null                   comment '订单号',
  member_id         bigint(20)      not null                   comment '会员ID',
  product_id        bigint(20)      not null                   comment '产品ID',
  product_name      varchar(100)    default ''                 comment '产品名称快照',
  currency          varchar(16)     not null default 'CNY'     comment 'CNY/USDT',
  price             decimal(18,4)   not null                   comment '认购价格',
  daily_rebate      decimal(18,4)   not null                   comment '每日返利',
  duration_days     int(11)         not null                   comment '总天数',
  remaining_days    int(11)         not null                   comment '剩余天数',
  last_rebate_date  date            default null               comment '上次返利日期',
  withdraw_required char(1)         default '0'                comment '是否提现指定产品',
  status            char(1)         default '0'                comment '状态（0持仓 1完成）',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (order_id),
  unique key uk_biz_order_no (order_no),
  key idx_biz_order_member (member_id, status)
) engine=innodb comment = '认购订单';

-- 返利记录
drop table if exists biz_rebate_log;
create table biz_rebate_log (
  rebate_id         bigint(20)      not null auto_increment    comment '返利ID',
  order_id          bigint(20)      not null                   comment '订单ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null default 'CNY'     comment 'CNY/USDT',
  amount            decimal(18,4)   not null                   comment '返利金额',
  rebate_date       date            not null                   comment '返利日期',
  create_time       datetime                                   comment '创建时间',
  primary key (rebate_id),
  unique key uk_biz_rebate_order_date (order_id, rebate_date)
) engine=innodb comment = '产品返利记录';

-- 充值
drop table if exists biz_recharge;
create table biz_recharge (
  recharge_id       bigint(20)      not null auto_increment    comment '充值ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种',
  amount            decimal(18,4)   not null                   comment '金额',
  status            char(1)         default '0'                comment '状态（0待审 1通过 2拒绝）',
  audit_by          varchar(64)     default ''                 comment '审核人',
  audit_time        datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (recharge_id),
  key idx_biz_recharge_member (member_id, status)
) engine=innodb comment = '充值申请';

-- 提现
drop table if exists biz_withdraw;
create table biz_withdraw (
  withdraw_id       bigint(20)      not null auto_increment    comment '提现ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种',
  amount            decimal(18,4)   not null                   comment '金额',
  account_info      varchar(255)    default ''                 comment '收款信息（占位）',
  status            char(1)         default '0'                comment '状态（0待审 1通过 2拒绝）',
  audit_by          varchar(64)     default ''                 comment '审核人',
  audit_time        datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (withdraw_id),
  key idx_biz_withdraw_member (member_id, status)
) engine=innodb comment = '提现申请';

-- 分佣
drop table if exists biz_commission_log;
create table biz_commission_log (
  commission_id     bigint(20)      not null auto_increment    comment '分佣ID',
  from_member_id    bigint(20)      not null                   comment '来源会员',
  to_member_id      bigint(20)      not null                   comment '获得分佣会员',
  team_level        int(4)          not null                   comment '层级 1/2/3',
  currency          varchar(16)     not null                   comment '币种',
  base_amount       decimal(18,4)   not null                   comment '充值本金',
  rate              decimal(10,4)   not null                   comment '比例',
  amount            decimal(18,4)   not null                   comment '分佣金额',
  recharge_id       bigint(20)      default null               comment '充值单ID',
  create_time       datetime                                   comment '创建时间',
  primary key (commission_id),
  key idx_biz_commission_to (to_member_id),
  key idx_biz_commission_from (from_member_id)
) engine=innodb comment = '团队分佣记录';

-- 会员等级
drop table if exists biz_level;
create table biz_level (
  level_id            bigint(20)      not null auto_increment  comment '等级ID',
  level_name          varchar(50)     not null                 comment '等级名称',
  min_valid_members   int(11)         default 0                comment '最低有效会员人数',
  min_recharge_cny    decimal(18,4)   default 0                comment '最低累计充值(CNY)',
  min_recharge_usdt   decimal(18,4)   default 0                comment '最低累计充值(USDT，预留)',
  sort                int(4)          default 0                comment '排序(越大越高)',
  status              char(1)         default '0'              comment '状态（0正常 1停用）',
  create_by           varchar(64)     default ''               comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)     default ''               comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)    default null             comment '备注',
  primary key (level_id)
) engine=innodb auto_increment=1 comment = '会员等级';

-- 种子：等级
insert into biz_level values(1, 'V0', 0, 0, 0, 0, '0', 'admin', sysdate(), '', null, '默认等级');
insert into biz_level values(2, 'V1', 3, 1000, 0, 1, '0', 'admin', sysdate(), '', null, '占位阈值，可后台调整');
insert into biz_level values(3, 'V2', 10, 5000, 0, 2, '0', 'admin', sysdate(), '', null, '占位阈值，可后台调整');
insert into biz_level values(4, 'V3', 30, 20000, 0, 3, '0', 'admin', sysdate(), '', null, '占位阈值，可后台调整');

-- 种子：提现指定产品
insert into biz_product values(1, '提现指定产品', 'CNY', 100.0000, 5.0000, 30, '1', '0', 1, 'admin', sysdate(), '', null, '认购后才可提现');
insert into biz_product values(2, 'USDT Product', 'USDT', 100.0000, 5.0000, 30, '1', '0', 2, 'admin', sysdate(), '', null, 'USDT withdraw required');

-- 业务参数（可重复执行）
delete from sys_config where config_id between 20 and 38;
insert into sys_config values(20, '签到奖励金额', 'biz.checkin.amount', '2', 'N', 'admin', sysdate(), '', null, '每日签到奖励人民币金额');
insert into sys_config values(21, '提现最低金额', 'biz.withdraw.minAmount', '105', 'N', 'admin', sysdate(), '', null, '人民币最低提现金额');
insert into sys_config values(22, '团队一级分佣比例', 'biz.team.rate.l1', '9', 'N', 'admin', sysdate(), '', null, '充值一级分佣百分比');
insert into sys_config values(23, '团队二级分佣比例', 'biz.team.rate.l2', '3', 'N', 'admin', sysdate(), '', null, '充值二级分佣百分比');
insert into sys_config values(24, '团队三级分佣比例', 'biz.team.rate.l3', '1', 'N', 'admin', sysdate(), '', null, '充值三级分佣百分比');
insert into sys_config values(25, '邀请奖励金额', 'biz.invite.reward', '0', 'N', 'admin', sysdate(), '', null, '邀请好友奖励，0表示暂无奖励');
insert into sys_config values(26, 'USDT业务开关', 'biz.usdt.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示USDT充提暂未开放');
insert into sys_config values(27, 'USDT min withdraw', 'biz.withdraw.minAmount.usdt', '105', 'N', 'admin', sysdate(), '', null, 'USDT min withdraw');
insert into sys_config values(28, '签到第一档连续天数', 'biz.checkin.prize1.days', '180', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(29, '签到第一档奖品', 'biz.checkin.prize1.name', '华为手机', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(30, '签到第一档中奖概率', 'biz.checkin.prize1.rate', '1', 'N', 'admin', sysdate(), '', null, '百分数，1表示1%，100表示必中');
insert into sys_config values(31, '签到第一档开关', 'biz.checkin.prize1.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');
insert into sys_config values(32, '签到第二档连续天数', 'biz.checkin.prize2.days', '365', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(33, '签到第二档奖品', 'biz.checkin.prize2.name', '华硕ROG笔记本电脑', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(34, '签到第二档中奖概率', 'biz.checkin.prize2.rate', '0.5', 'N', 'admin', sysdate(), '', null, '百分数，0.5表示0.5%，100表示必中');
insert into sys_config values(35, '签到第二档开关', 'biz.checkin.prize2.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');
insert into sys_config values(36, '谷歌验证开关', 'biz.google.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭谷歌验证');
insert into sys_config values(37, '提现必须谷歌验证', 'biz.google.requireWithdraw', 'true', 'N', 'admin', sysdate(), '', null, 'true表示未绑定不能提现');
insert into sys_config values(38, '谷歌验证器名称', 'biz.google.issuer', 'App', 'N', 'admin', sysdate(), '', null, '显示在谷歌验证器中的名称');

-- 每日返利任务（默认开启）
delete from sys_job where job_id = 100;
insert into sys_job values(100, '产品每日返利', 'DEFAULT', 'dailyRebateTask.execute()', '0 5 0 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '持仓订单每日返利');


-- App首页运行概览（展示用，后台手改）
drop table if exists biz_overview;
create table biz_overview (
  item_id           bigint(20)      not null auto_increment    comment '卡片ID',
  item_key          varchar(32)     not null                   comment '卡片标识，App用它匹配本地图',
  title             varchar(64)     not null                   comment '标题',
  display_value     varchar(64)     not null                   comment '展示数值，含单位',
  status_text       varchar(64)     default ''                 comment '状态文案',
  status_color      varchar(16)     default '#4DA3FF'          comment '状态点颜色',
  image_url         varchar(500)    default ''                 comment '可选配图，空则App用本地图',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (item_id),
  unique key uk_biz_overview_key (item_key)
) engine=innodb comment = 'App运行概览';

insert into biz_overview values(1, 'satellite', '在轨卫星', '320 颗', '正常运行', '#3DDC84', '', 1, '0', 'admin', sysdate(), '', null, null);
insert into biz_overview values(2, 'coverage', '覆盖国家/地区', '150+', '正常运行', '#4DA3FF', '', 2, '0', 'admin', sysdate(), '', null, null);
insert into biz_overview values(3, 'terminal', '在线终端', '1256000+', '稳定连接', '#4DA3FF', '', 3, '0', 'admin', sysdate(), '', null, null);


-- App关于我们（展示用，后台手改）
drop table if exists biz_about;
create table biz_about (
  about_id          bigint(20)      not null auto_increment    comment '内容ID',
  title             varchar(100)    not null                   comment '标题',
  subtitle          varchar(200)    default ''                 comment '副标题',
  content           mediumtext                                 comment '正文，后台富文本',
  image_url         varchar(500)    default ''                 comment '可选配图',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (about_id)
) engine=innodb comment = 'App关于我们';

insert into biz_about values(1, '星帆智联', '连接星空 · 智联未来', '<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>', '', 1, '0', 'admin', sysdate(), '', null, null);


-- App官方群聊（展示用，后台上传二维码）
drop table if exists biz_group_chat;
create table biz_group_chat (
  group_id          bigint(20)      not null auto_increment    comment '群聊ID',
  title             varchar(100)    not null                   comment '标题',
  hint              varchar(100)    default '扫码进群'          comment '二维码下方提示',
  qr_url            varchar(500)    default ''                 comment '群聊二维码图片地址',
  remark            varchar(500)    default ''                 comment '补充说明',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (group_id)
) engine=innodb comment = 'App官方群聊';

insert into biz_group_chat values(1, '官方群聊', '扫码进群', '', '', 1, '0', 'admin', sysdate(), '', null);


-- App新闻资讯（展示用，后台手改）
drop table if exists biz_news;
create table biz_news (
  news_id           bigint(20)      not null auto_increment    comment '新闻ID',
  title             varchar(200)    not null                   comment '标题',
  summary           varchar(500)    default ''                 comment '摘要',
  cover_url         varchar(500)    default ''                 comment '封面图',
  content           mediumtext                                 comment '正文，后台富文本',
  publish_time      datetime                                   comment '发布日期',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (news_id)
) engine=innodb comment = 'App新闻资讯';

insert into biz_news values(1, '俄罗斯近24小时遥感卫星观测任务与行业应用动态', '俄罗斯近24小时遥感卫星观测任务与行业应用动态', '', '<p>一、在轨遥感星座整体运行工况平稳</p><p>（一）高分辨率光学卫星完成农情、地质重点区域成像。</p><p>（二）雷达卫星持续开展全天候云雨覆盖区域观测。</p><p>二、行业应用动态</p><p>面向应急、农业、交通等场景的数据产品按计划分发，支撑多地业务系统稳定运行。</p>', '2026-08-18 00:00:00', 1, '0', 'admin', sysdate(), '', null, null);
insert into biz_news values(2, '商业航天星座组网加速，行业应用场景持续拓展', '商业航天星座组网加速，行业应用场景持续拓展', '', '<p>商业航天正从单星验证走向规模组网。星帆智联持续推进星座部署与地面终端协同，为行业用户提供稳定连接能力。</p>', '2026-08-12 00:00:00', 2, '0', 'admin', sysdate(), '', null, null);

-- ----------------------------
-- 业务菜单
-- ----------------------------
delete from sys_role_menu where menu_id >= 2000;
delete from sys_menu where menu_id >= 2000;
insert into sys_menu values('2000', '业务管理', '0', '5', 'biz', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, '认购返利业务目录');
insert into sys_menu values('2001', '会员管理', '2000', '1', 'member', 'biz/member/index', '', '', 1, 0, 'C', '0', '0', 'biz:member:list', 'user', 'admin', sysdate(), '', null, 'C端会员');
insert into sys_menu values('2002', '产品管理', '2000', '2', 'product', 'biz/product/index', '', '', 1, 0, 'C', '0', '0', 'biz:product:list', 'shopping', 'admin', sysdate(), '', null, '认购产品');
insert into sys_menu values('2003', '认购订单', '2000', '3', 'order', 'biz/order/index', '', '', 1, 0, 'C', '0', '0', 'biz:order:list', 'list', 'admin', sysdate(), '', null, '认购订单');
insert into sys_menu values('2004', '签到记录', '2000', '4', 'checkin', 'biz/checkin/index', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:list', 'date', 'admin', sysdate(), '', null, '签到记录');
insert into sys_menu values('2005', '充值审核', '2000', '5', 'recharge', 'biz/recharge/index', '', '', 1, 0, 'C', '0', '0', 'biz:recharge:list', 'edit', 'admin', sysdate(), '', null, '充值审核');
insert into sys_menu values('2006', '提现审核', '2000', '6', 'withdraw', 'biz/withdraw/index', '', '', 1, 0, 'C', '0', '0', 'biz:withdraw:list', 'edit', 'admin', sysdate(), '', null, '提现审核');
insert into sys_menu values('2007', '资金流水', '2000', '7', 'walletLog', 'biz/walletLog/index', '', '', 1, 0, 'C', '0', '0', 'biz:walletLog:list', 'log', 'admin', sysdate(), '', null, '资金流水');
insert into sys_menu values('2008', '团队关系', '2000', '8', 'team', 'biz/team/index', '', '', 1, 0, 'C', '0', '0', 'biz:team:list', 'tree', 'admin', sysdate(), '', null, '团队关系');
insert into sys_menu values('2009', '会员等级', '2000', '9', 'level', 'biz/level/index', '', '', 1, 0, 'C', '0', '0', 'biz:level:list', 'peoples', 'admin', sysdate(), '', null, '会员等级');
insert into sys_menu values('2010', '分佣记录', '2000', '10', 'commission', 'biz/commission/index', '', '', 1, 0, 'C', '0', '0', 'biz:commission:list', 'form', 'admin', sysdate(), '', null, '分佣记录');
insert into sys_menu values('2011', '签到规则', '2000', '4', 'checkinRule', 'biz/checkin/rule', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:rule', 'edit', 'admin', sysdate(), '', null, '签到金额与连续抽奖规则');
insert into sys_menu values('2012', '签到中奖', '2000', '4', 'checkinPrize', 'biz/checkin/prize', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:prize', 'star', 'admin', sysdate(), '', null, '连续签到抽奖记录');
insert into sys_menu values('2013', '运行概览', '2000', '0', 'overview', 'biz/overview/index', '', '', 1, 0, 'C', '0', '0', 'biz:overview:list', 'dashboard', 'admin', sysdate(), '', null, 'App首页展示数字，后台手改');
insert into sys_menu values('2014', '关于我们', '2000', '0', 'about', 'biz/about/index', '', '', 1, 0, 'C', '0', '0', 'biz:about:list', 'guide', 'admin', sysdate(), '', null, 'App关于我们，后台手改');
insert into sys_menu values('2015', '官方群聊', '2000', '0', 'groupChat', 'biz/groupChat/index', '', '', 1, 0, 'C', '0', '0', 'biz:group:list', 'message', 'admin', sysdate(), '', null, 'App官方群聊二维码，后台手改');
insert into sys_menu values('2016', '新闻资讯', '2000', '0', 'news', 'biz/news/index', '', '', 1, 0, 'C', '0', '0', 'biz:news:list', 'documentation', 'admin', sysdate(), '', null, 'App新闻资讯，后台手改');

-- 按钮权限
insert into sys_menu values('2101', '会员查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2102', '会员修改', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2103', 'Member Add', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2111', '产品查询', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2112', '产品新增', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2113', '产品修改', '2002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2114', '产品删除', '2002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2121', '订单查询', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:order:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2131', '签到查询', '2004', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2132', '签到规则保存', '2011', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:rule', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2133', '签到中奖查询', '2012', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:prize', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2141', '充值查询', '2005', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:recharge:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2142', '充值新增', '2005', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:recharge:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2143', '充值审核', '2005', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:recharge:audit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2151', '提现查询', '2006', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:withdraw:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2152', '提现审核', '2006', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:withdraw:audit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2161', '流水查询', '2007', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletLog:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2171', '团队查询', '2008', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2181', '等级查询', '2009', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2182', '等级新增', '2009', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2183', '等级修改', '2009', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2184', '等级删除', '2009', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2191', '分佣查询', '2010', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:commission:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2201', '概览查询', '2013', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2202', '概览新增', '2013', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2203', '概览修改', '2013', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2204', '概览删除', '2013', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2211', '关于查询', '2014', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2212', '关于新增', '2014', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2213', '关于修改', '2014', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2214', '关于删除', '2014', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2221', '群聊查询', '2015', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2222', '群聊新增', '2015', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2223', '群聊修改', '2015', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2224', '群聊删除', '2015', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2231', '新闻查询', '2016', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2232', '新闻新增', '2016', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2233', '新闻修改', '2016', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2234', '新闻删除', '2016', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:remove', '#', 'admin', sysdate(), '', null, '');
