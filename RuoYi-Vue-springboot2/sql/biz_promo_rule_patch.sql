-- 注册/实名/推广奖励规则（可重复执行：表已存在会报错可忽略）

create table if not exists biz_promo_grant (
  grant_id          bigint(20)      not null auto_increment    comment '发放ID',
  member_id         bigint(20)      not null                   comment '收款会员',
  from_member_id    bigint(20)      not null                   comment '来源会员：自领=本人，推广=被邀请人',
  grant_type        varchar(20)     not null                   comment 'KYC_SELF实名自领 INVITE推广奖励',
  currency          varchar(10)     default 'CNY'              comment '币种',
  amount            decimal(18,4)   default 0                  comment '金额',
  status            char(1)         default '1'                comment '1已发放',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (grant_id),
  unique key uk_promo_type_from (grant_type, from_member_id),
  key idx_promo_member (member_id)
) engine=innodb comment = '实名注册与推广奖励发放';

delete from sys_config where config_id between 61 and 74;
delete from sys_config where config_key in (
  'biz.promo.enabled','biz.promo.kycSelf.enabled','biz.promo.kycSelf.cny','biz.promo.kycSelf.usdt',
  'biz.promo.invite.enabled','biz.promo.invite.amount','biz.promo.invite.currency','biz.promo.invite.lockParent',
  'biz.team.enabled','biz.promo.ruleText'
);
insert into sys_config values(61, '推广规则总开关', 'biz.promo.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false关闭实名自领和邀请奖励');
insert into sys_config values(62, '实名注册奖励开关', 'biz.promo.kycSelf.enabled', 'true', 'N', 'admin', sysdate(), '', null, '实名后可选CNY或USDT领一次');
insert into sys_config values(63, '实名注册奖励CNY', 'biz.promo.kycSelf.cny', '14', 'N', 'admin', sysdate(), '', null, '实名注册奖励人民币金额');
insert into sys_config values(64, '实名注册奖励USDT', 'biz.promo.kycSelf.usdt', '2', 'N', 'admin', sysdate(), '', null, '实名注册奖励USDT金额');
insert into sys_config values(65, '实名推广奖励开关', 'biz.promo.invite.enabled', 'true', 'N', 'admin', sysdate(), '', null, '被邀请人实名后给邀请人发奖');
insert into sys_config values(66, '实名推广奖励金额', 'biz.promo.invite.amount', '2', 'N', 'admin', sysdate(), '', null, '每成功邀请1名实名用户的奖励');
insert into sys_config values(67, '实名推广奖励币种', 'biz.promo.invite.currency', 'CNY', 'N', 'admin', sysdate(), '', null, '邀请奖励币种 CNY或USDT');
insert into sys_config values(68, '邀请后不可改上级', 'biz.promo.invite.lockParent', 'true', 'N', 'admin', sysdate(), '', null, '注册时绑定邀请码后不可转移');
insert into sys_config values(69, '团队返佣开关', 'biz.team.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false关闭充值三级返佣');
insert into sys_config values(70, '注册推广规则说明', 'biz.promo.ruleText', '用户注册与推广奖励规则：
一、实名注册奖励
新用户完成注册并通过实名认证后，可获得 14 元或 2 USDT 平台余额，两种奖励方式任选其一。
二、实名推广奖励
每成功邀请 1 名新用户完成实名注册，邀请人可获得 2 元推广奖励。上下级不可以转移，请核对好正确的邀请码再注册。
三、团队返佣机制
一级返佣 9%、二级返佣 3%、三级返佣 1%

奖励资格、返佣计算及发放结果以平台系统实际核算为准；如发现异常注册、批量账户或其他违规行为，平台有权取消相关奖励资格。', 'N', 'admin', sysdate(), '', null, 'App邀请/规则页展示全文');

update sys_config set config_value = '2', remark = '每成功邀请1名实名用户给邀请人的金额' where config_key = 'biz.invite.reward';

delete from sys_menu where menu_id in (2023, 2295, 2296);
insert into sys_menu values('2023', '注册推广规则', '2000', '12', 'promo', 'biz/promo/index', '', '', 1, 0, 'C', '0', '0', 'biz:promo:query', 'peoples', 'admin', sysdate(), '', null, '实名注册奖励、邀请奖励与三级返佣');
insert into sys_menu values('2295', '推广规则查询', '2023', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:promo:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2296', '推广规则修改', '2023', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:promo:edit', '#', 'admin', sysdate(), '', null, '');
