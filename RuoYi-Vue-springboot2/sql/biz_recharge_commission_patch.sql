SET NAMES utf8mb4;
-- Recharge team commission: own switch, manual switch, rates. Repeatable.

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'recharge team enabled', 'biz.recharge.team.enabled', 'false', 'N', 'admin', sysdate(), 'false=off online/chain recharge commission'
from dual where not exists (select 1 from sys_config where config_key = 'biz.recharge.team.enabled');

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'recharge team manual enabled', 'biz.recharge.team.manual.enabled', 'false', 'N', 'admin', sysdate(), 'false=manual admin recharge does not commission'
from dual where not exists (select 1 from sys_config where config_key = 'biz.recharge.team.manual.enabled');

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'recharge team rate l1', 'biz.recharge.team.rate.l1', '9', 'N', 'admin', sysdate(), 'recharge L1 percent'
from dual where not exists (select 1 from sys_config where config_key = 'biz.recharge.team.rate.l1');

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'recharge team rate l2', 'biz.recharge.team.rate.l2', '3', 'N', 'admin', sysdate(), 'recharge L2 percent'
from dual where not exists (select 1 from sys_config where config_key = 'biz.recharge.team.rate.l2');

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'recharge team rate l3', 'biz.recharge.team.rate.l3', '1', 'N', 'admin', sysdate(), 'recharge L3 percent'
from dual where not exists (select 1 from sys_config where config_key = 'biz.recharge.team.rate.l3');

update sys_config set config_name='recharge team enabled', remark='false=off online/chain recharge commission'
where config_key='biz.recharge.team.enabled';
update sys_config set config_name='recharge team manual enabled', remark='false=manual admin recharge does not commission'
where config_key='biz.recharge.team.manual.enabled';
update sys_config set config_name='recharge team rate l1', remark='recharge L1 percent'
where config_key='biz.recharge.team.rate.l1';
update sys_config set config_name='recharge team rate l2', remark='recharge L2 percent'
where config_key='biz.recharge.team.rate.l2';
update sys_config set config_name='recharge team rate l3', remark='recharge L3 percent'
where config_key='biz.recharge.team.rate.l3';
