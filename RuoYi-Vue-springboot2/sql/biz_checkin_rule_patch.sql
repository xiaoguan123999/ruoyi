-- 签到规则配置化 + 连续签到抽奖（可重复执行）
create table if not exists biz_checkin_prize (
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

delete from sys_config where config_id between 28 and 35;
insert into sys_config values(28, '签到第一档连续天数', 'biz.checkin.prize1.days', '180', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(29, '签到第一档奖品', 'biz.checkin.prize1.name', '华为手机', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(30, '签到第一档中奖概率', 'biz.checkin.prize1.rate', '1', 'N', 'admin', sysdate(), '', null, '百分数，1表示1%，100表示必中');
insert into sys_config values(31, '签到第一档开关', 'biz.checkin.prize1.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');
insert into sys_config values(32, '签到第二档连续天数', 'biz.checkin.prize2.days', '365', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(33, '签到第二档奖品', 'biz.checkin.prize2.name', '华硕ROG笔记本电脑', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(34, '签到第二档中奖概率', 'biz.checkin.prize2.rate', '0.5', 'N', 'admin', sysdate(), '', null, '百分数，0.5表示0.5%，100表示必中');
insert into sys_config values(35, '签到第二档开关', 'biz.checkin.prize2.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');

delete from sys_menu where menu_id in (2011, 2012, 2132, 2133);
insert into sys_menu values('2011', '签到规则', '2000', '4', 'checkinRule', 'biz/checkin/rule', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:rule', 'edit', 'admin', sysdate(), '', null, '签到金额与连续抽奖规则');
insert into sys_menu values('2012', '签到中奖', '2000', '4', 'checkinPrize', 'biz/checkin/prize', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:prize', 'star', 'admin', sysdate(), '', null, '连续签到抽奖记录');
insert into sys_menu values('2132', '签到规则保存', '2011', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:rule', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2133', '签到中奖查询', '2012', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:prize', '#', 'admin', sysdate(), '', null, '');
