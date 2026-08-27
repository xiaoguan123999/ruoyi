SET NAMES utf8mb4;
-- 实名认证奖励菜单，挂在会员中心（可重复执行）

delete from sys_role_menu where menu_id in (2035, 2333, 2334);
delete from sys_menu where menu_id in (2035, 2333, 2334);

insert into sys_menu values('2035', '实名认证奖励', '2297', '10', 'kycReward', 'biz/kycReward/index', '', '', 1, 0, 'C', '0', '0', 'biz:kycReward:query', 'money', 'admin', sysdate(), '', null, '实名后自选人民币或USDT领取一次');
insert into sys_menu values('2333', '实名奖励查询', '2035', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:kycReward:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2334', '实名奖励修改', '2035', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:kycReward:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2035 as menu_id union all select 2333 union all select 2334
) m
where rm.menu_id = 2001
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

insert ignore into sys_role_menu values (1, 2035), (1, 2333), (1, 2334);
