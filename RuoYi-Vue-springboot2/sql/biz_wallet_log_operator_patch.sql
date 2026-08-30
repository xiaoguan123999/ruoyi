SET NAMES utf8mb4;
-- 资金流水加操作人：后台调账=后台账号，App充值=会员手机号。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_wallet_log' and column_name = 'operator'
);
set @sql := if(@exist = 0,
  'alter table biz_wallet_log add column operator varchar(64) default '''' comment ''操作人'' after remark',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_wallet_log
set operator = trim(substring_index(substring_index(remark, ': ', 1), '调账 ', -1))
where biz_type = 'ADJUST'
  and remark like '调账 %: %'
  and ifnull(operator, '') = '';

update biz_wallet_log l
inner join biz_member m on m.member_id = l.member_id
set l.operator = ifnull(m.phone, '')
where l.biz_type in ('RECHARGE', 'SUBSCRIBE', 'WITHDRAW_FREEZE', 'WITHDRAW_SUCCESS', 'WITHDRAW_REJECT')
  and ifnull(l.operator, '') = '';

update biz_wallet_log
set operator = 'system'
where ifnull(operator, '') = ''
  and biz_type not in ('ADJUST', 'RECHARGE', 'SUBSCRIBE', 'WITHDRAW_FREEZE', 'WITHDRAW_SUCCESS', 'WITHDRAW_REJECT');

update biz_wallet_log
set operator = 'admin'
where biz_type = 'ADJUST' and ifnull(operator, '') = '';
