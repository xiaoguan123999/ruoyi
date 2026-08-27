SET NAMES utf8mb4;
-- 提现手续费：从申请金额扣除，到账=申请金额-手续费（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_withdraw'
    and column_name = 'fee_amount'
);
set @sql := if(@exist = 0,
  'alter table biz_withdraw add column fee_amount decimal(18,4) default 0 comment ''手续费'' after amount',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_withdraw'
    and column_name = 'arrival_amount'
);
set @sql := if(@exist = 0,
  'alter table biz_withdraw add column arrival_amount decimal(18,4) default 0 comment ''到账金额'' after fee_amount',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_withdraw
   set fee_amount = ifnull(fee_amount, 0)
 where fee_amount is null;

update biz_withdraw
   set arrival_amount = amount
 where arrival_amount is null or arrival_amount = 0;

delete from sys_config where config_id = 81 or config_key = 'biz.withdraw.feeRate';
insert into sys_config values(81, '提现手续费比例', 'biz.withdraw.feeRate', '3', 'N', 'admin', sysdate(), '', null, '百分数，3表示3%，0表示免手续费');
