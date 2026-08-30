SET NAMES utf8mb4;
-- Withdraw requires KYC: default false (same as current, no check). Safe to re-run.

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'Withdraw need KYC', 'biz.withdraw.needKyc', 'false', 'N', 'admin', sysdate(), 'true = must finish KYC before withdraw'
from dual
where not exists (select 1 from sys_config where config_key = 'biz.withdraw.needKyc');
