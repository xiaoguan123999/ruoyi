SET NAMES utf8mb4;
-- USDT-BEP20 (BSC) alongside USDT-TRC20. Re-runnable.

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_member'
    and column_name = 'chain_address_bep20'
);
set @sql := if(@exist = 0,
  'alter table biz_member add column chain_address_bep20 varchar(128) default '''' comment ''USDT-BEP20 collect address for downline'' after chain_address',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_chain_deposit'
    and column_name = 'network'
);
set @sql := if(@exist = 0,
  'alter table biz_chain_deposit add column network varchar(16) not null default ''TRC20'' comment ''TRC20 or BEP20'' after asset',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_chain_deposit set network = 'TRC20' where ifnull(network, '') = '';

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select * from (
  select 'BSC chain deposit switch' as config_name, 'biz.chain.bsc.enabled' as config_key,
         'true' as config_value, 'N' as config_type, 'admin' as create_by, sysdate() as create_time,
         'false to hide BEP20 on App; TRC20 uses biz.chain.tron.enabled' as remark
  union all
  select 'BSC collection address', 'biz.chain.bsc.address', '', 'N', 'admin', sysdate(),
         'shared USDT-BEP20 receive address, fill in admin config'
  union all
  select 'BscScan API key', 'biz.chain.bsc.apiKey', '', 'N', 'admin', sysdate(),
         'BscScan / Etherscan API key for tokentx'
  union all
  select 'BscScan API URL', 'biz.chain.bsc.apiUrl', '', 'N', 'admin', sysdate(),
         'empty = https://api.bscscan.com/api ; Etherscan v2 = https://api.etherscan.io/v2/api'
) t
where not exists (select 1 from sys_config c where c.config_key = t.config_key);

update sys_job
   set job_name = 'USDT chain deposit scan',
       remark = 'poll TronGrid USDT-TRC20 and BscScan USDT-BEP20 by amount fingerprint'
 where job_id = 102;
