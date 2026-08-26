SET NAMES utf8mb4;
-- 会员管理：重置登录密码 / 重置交易密码（可重复执行）

delete from sys_role_menu where menu_id in (2104, 2105);
delete from sys_menu where menu_id in (2104, 2105);

insert into sys_menu values('2104', '重置登录密码', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:resetPwd', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2105', '重置交易密码', '2001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:resetPayPwd', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2104 as menu_id union all select 2105
) m
where rm.menu_id = 2102
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);
