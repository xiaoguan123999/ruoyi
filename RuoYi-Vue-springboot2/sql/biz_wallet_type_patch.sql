SET NAMES utf8mb4;
-- 钱包类型：余额 / 产品收益 / 推广收益 / 助力值。CNY、USDT 是类型下的币种。
-- 可重复执行。历史余额拆分只在尚未存在 PRODUCT 钱包时做一次。

create table if not exists biz_wallet_type (
  type_id           bigint(20)      not null auto_increment    comment '类型ID',
  type_code         varchar(32)     not null                   comment '类型编码',
  type_name         varchar(64)     not null                   comment '类型名称',
  withdraw_mode     varchar(32)     not null default 'NONE'    comment '提现规则 NONE不可提 OPEN可提 ANY_ORDER任意认购后 PRODUCT_REQUIRED指定产品',
  status            char(1)         default '0'                comment '状态 0正常 1停用',
  sort              int(4)          default 0                  comment '排序，越小越靠前',
  builtin           char(1)         default '0'                comment '内置 1不可删改编码',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (type_id),
  unique key uk_biz_wallet_type_code (type_code)
) engine=innodb comment = '钱包类型';

create table if not exists biz_wallet_credit_rule (
  rule_id           bigint(20)      not null auto_increment    comment '规则ID',
  biz_type          varchar(32)     not null                   comment '业务类型',
  biz_name          varchar(64)     not null                   comment '业务名称',
  type_code         varchar(32)     not null                   comment '入账钱包类型',
  builtin           char(1)         default '0'                comment '内置 1不可删除',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (rule_id),
  unique key uk_biz_wallet_credit_biz (biz_type)
) engine=innodb comment = '奖励入账钱包配置';

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'BALANCE', '余额', 'NONE', '0', 1, '1', 'admin', sysdate(), '充值进去的钱，不能提现，用于认购'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'BALANCE');

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'PRODUCT', '产品收益', 'PRODUCT_REQUIRED', '0', 2, '1', 'admin', sysdate(), '产品产生的收益，可以提现'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'PRODUCT');

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'PROMO', '推广收益', 'ANY_ORDER', '0', 3, '1', 'admin', sysdate(), '推广相关收益，认购任意产品后才能提现'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'PROMO');

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'ASSIST', '助力值', 'NONE', '0', 4, '1', 'admin', sysdate(), '暂定，不可提现'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'ASSIST');

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'RECHARGE', '充值', 'BALANCE', '1', 1, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'RECHARGE');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'REBATE', '产品日返', 'PRODUCT', '1', 2, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'REBATE');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'CHECKIN', '签到奖励', 'PROMO', '1', 3, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'CHECKIN');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'KYC_REWARD', '实名认证奖励', 'PROMO', '1', 4, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'KYC_REWARD');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'INVITE', '邀请奖励', 'PROMO', '1', 5, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'INVITE');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'COMMISSION', '推广分佣', 'PROMO', '1', 6, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'COMMISSION');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'LEVEL_REWARD', '等级奖励', 'PROMO', '1', 7, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'LEVEL_REWARD');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'ADJUST', '后台调账默认', 'BALANCE', '1', 8, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'ADJUST');

drop procedure if exists biz_patch_wallet_type;

delimiter $$

create procedure biz_patch_wallet_type()
begin
  if not exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_wallet' and column_name = 'type_code') then
    alter table biz_wallet add column type_code varchar(32) not null default 'BALANCE' comment '钱包类型编码' after member_id;
  end if;
  if exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_wallet' and index_name = 'uk_biz_wallet_member_currency') then
    alter table biz_wallet drop index uk_biz_wallet_member_currency;
  end if;
  if not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_wallet' and index_name = 'uk_biz_wallet_member_type_currency') then
    alter table biz_wallet add unique key uk_biz_wallet_member_type_currency (member_id, type_code, currency);
  end if;
  if not exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_wallet_log' and column_name = 'type_code') then
    alter table biz_wallet_log add column type_code varchar(32) not null default 'BALANCE' comment '钱包类型编码' after member_id;
  end if;
  if not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_wallet_log' and index_name = 'idx_biz_wallet_log_type') then
    alter table biz_wallet_log add key idx_biz_wallet_log_type (member_id, type_code, create_time);
  end if;
  if not exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_withdraw' and column_name = 'wallet_type_code') then
    alter table biz_withdraw add column wallet_type_code varchar(32) default 'PRODUCT' comment '提现钱包类型' after currency;
  end if;

  update biz_withdraw
     set wallet_type_code = 'PROMO'
   where ifnull(wallet_type_code, '') in ('', 'PRODUCT')
     and (
       ifnull(remark, '') like '推广收益%'
       or upper(ifnull(remark, '')) like 'PROMO%'
       or upper(ifnull(remark, '')) like 'ASSIST%'
     );

  update biz_withdraw
     set wallet_type_code = 'PRODUCT'
   where ifnull(wallet_type_code, '') = '';

  update biz_wallet_log l
     set l.type_code = case
       when l.biz_type = 'REBATE' then 'PRODUCT'
       when l.biz_type in ('CHECKIN', 'KYC_REWARD', 'INVITE', 'COMMISSION', 'LEVEL_REWARD') then 'PROMO'
       when l.biz_type in ('WITHDRAW_FREEZE', 'WITHDRAW_SUCCESS', 'WITHDRAW_REJECT') then ifnull((
         select w.wallet_type_code from biz_withdraw w where w.withdraw_id = l.biz_id
       ), 'PRODUCT')
       else 'BALANCE'
     end
   where ifnull(l.type_code, 'BALANCE') = 'BALANCE'
     and l.biz_type in ('REBATE', 'CHECKIN', 'KYC_REWARD', 'INVITE', 'COMMISSION', 'LEVEL_REWARD',
                        'WITHDRAW_FREEZE', 'WITHDRAW_SUCCESS', 'WITHDRAW_REJECT');

  if (select count(*) from biz_wallet where type_code = 'PRODUCT') = 0 then
    drop temporary table if exists tmp_wallet_split;
    create temporary table tmp_wallet_split (
      member_id bigint(20) not null,
      currency varchar(16) not null,
      old_available decimal(18,4) not null default 0,
      old_frozen decimal(18,4) not null default 0,
      product_ideal decimal(18,4) not null default 0,
      promo_ideal decimal(18,4) not null default 0,
      product_frozen decimal(18,4) not null default 0,
      promo_frozen decimal(18,4) not null default 0,
      product_avail decimal(18,4) not null default 0,
      promo_avail decimal(18,4) not null default 0,
      balance_avail decimal(18,4) not null default 0,
      balance_frozen decimal(18,4) not null default 0,
      primary key (member_id, currency)
    );

    insert into tmp_wallet_split (member_id, currency, old_available, old_frozen, product_ideal, promo_ideal, product_frozen, promo_frozen)
    select w.member_id, w.currency, w.available, w.frozen,
           greatest(ifnull((
             select sum(case
               when l.biz_type = 'REBATE' then l.amount
               when l.biz_type in ('WITHDRAW_FREEZE', 'WITHDRAW_REJECT')
                    and l.available_before <> l.available_after
                    and ifnull((select wd.wallet_type_code from biz_withdraw wd where wd.withdraw_id = l.biz_id), 'PRODUCT') = 'PRODUCT'
               then l.amount
               else 0 end)
             from biz_wallet_log l
             where l.member_id = w.member_id and l.currency = w.currency
           ), 0), 0),
           greatest(ifnull((
             select sum(case
               when l.biz_type in ('CHECKIN', 'KYC_REWARD', 'INVITE', 'COMMISSION', 'LEVEL_REWARD') then l.amount
               when l.biz_type in ('WITHDRAW_FREEZE', 'WITHDRAW_REJECT')
                    and l.available_before <> l.available_after
                    and ifnull((select wd.wallet_type_code from biz_withdraw wd where wd.withdraw_id = l.biz_id), 'PRODUCT') = 'PROMO'
               then l.amount
               else 0 end)
             from biz_wallet_log l
             where l.member_id = w.member_id and l.currency = w.currency
           ), 0), 0),
           greatest(ifnull((
             select sum(wd.amount) from biz_withdraw wd
             where wd.member_id = w.member_id and wd.currency = w.currency
               and wd.status = '0' and ifnull(wd.wallet_type_code, 'PRODUCT') = 'PRODUCT'
           ), 0), 0),
           greatest(ifnull((
             select sum(wd.amount) from biz_withdraw wd
             where wd.member_id = w.member_id and wd.currency = w.currency
               and wd.status = '0' and ifnull(wd.wallet_type_code, 'PRODUCT') = 'PROMO'
           ), 0), 0)
    from biz_wallet w
    where ifnull(w.type_code, 'BALANCE') = 'BALANCE';

    update tmp_wallet_split
       set product_frozen = least(product_frozen, old_frozen),
           promo_frozen = least(promo_frozen, greatest(old_frozen - least(product_frozen, old_frozen), 0)),
           balance_frozen = greatest(old_frozen - least(product_frozen, old_frozen) - least(promo_frozen, greatest(old_frozen - least(product_frozen, old_frozen), 0)), 0),
           product_avail = least(product_ideal, old_available),
           promo_avail = least(promo_ideal, greatest(old_available - least(product_ideal, old_available), 0)),
           balance_avail = greatest(old_available - least(product_ideal, old_available) - least(promo_ideal, greatest(old_available - least(product_ideal, old_available), 0)), 0);

    update biz_wallet w
      join tmp_wallet_split t on t.member_id = w.member_id and t.currency = w.currency
       set w.available = t.balance_avail, w.frozen = t.balance_frozen, w.update_time = sysdate()
     where ifnull(w.type_code, 'BALANCE') = 'BALANCE';

    insert into biz_wallet (member_id, type_code, currency, available, frozen, create_time, update_time)
    select t.member_id, 'PRODUCT', t.currency, t.product_avail, t.product_frozen, sysdate(), sysdate()
    from tmp_wallet_split t
    where not exists (
      select 1 from biz_wallet x where x.member_id = t.member_id and x.type_code = 'PRODUCT' and x.currency = t.currency
    );

    insert into biz_wallet (member_id, type_code, currency, available, frozen, create_time, update_time)
    select t.member_id, 'PROMO', t.currency, t.promo_avail, t.promo_frozen, sysdate(), sysdate()
    from tmp_wallet_split t
    where not exists (
      select 1 from biz_wallet x where x.member_id = t.member_id and x.type_code = 'PROMO' and x.currency = t.currency
    );
  end if;

  insert into biz_wallet (member_id, type_code, currency, available, frozen, create_time, update_time)
  select m.member_id, t.type_code, c.currency, 0, 0, sysdate(), sysdate()
  from biz_member m
  cross join (select 'BALANCE' as type_code union all select 'PRODUCT' union all select 'PROMO' union all select 'ASSIST') t
  cross join (select 'CNY' as currency union all select 'USDT') c
  where not exists (
    select 1 from biz_wallet w
    where w.member_id = m.member_id and w.type_code = t.type_code and w.currency = c.currency
  );
end

$$
delimiter ;
call biz_patch_wallet_type();
drop procedure if exists biz_patch_wallet_type;

delete from sys_role_menu where menu_id in (2036, 2037, 2340, 2341, 2342, 2343, 2344, 2345, 2346, 2347);
delete from sys_menu where menu_id in (2036, 2037, 2340, 2341, 2342, 2343, 2344, 2345, 2346, 2347);

insert into sys_menu values('2036', '钱包类型', '2032', '13', 'walletType', 'biz/walletType/index', '', '', 1, 0, 'C', '0', '0', 'biz:walletType:list', 'nested', 'admin', sysdate(), '', null, '余额、产品收益、推广收益、助力值，可新增');
insert into sys_menu values('2340', '钱包类型查询', '2036', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2341', '钱包类型新增', '2036', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2342', '钱包类型修改', '2036', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2343', '钱包类型删除', '2036', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('2037', '奖励入账', '2032', '14', 'walletCredit', 'biz/walletCredit/index', '', '', 1, 0, 'C', '0', '0', 'biz:walletCredit:list', 'edit', 'admin', sysdate(), '', null, '签到、实名、邀请等奖励入到哪个钱包');
insert into sys_menu values('2344', '入账配置查询', '2037', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2345', '入账配置新增', '2037', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2346', '入账配置修改', '2037', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2347', '入账配置删除', '2037', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2036 as menu_id union all select 2037 union all select 2340 union all select 2341
  union all select 2342 union all select 2343 union all select 2344 union all select 2345
  union all select 2346 union all select 2347
) m
where rm.menu_id = 2007
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

insert ignore into sys_role_menu values
 (1, 2036), (1, 2037), (1, 2340), (1, 2341), (1, 2342), (1, 2343), (1, 2344), (1, 2345), (1, 2346), (1, 2347);

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PRODUCT', '产品收益提现', 'PRODUCT', '1', 9, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PRODUCT');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PROMO', '推广收益提现', 'PROMO', '1', 10, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PROMO');
