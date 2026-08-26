SET NAMES utf8mb4;
-- 后台钱包调账按钮（可重复执行，只动 2162）

delete from sys_role_menu where menu_id = 2162;
delete from sys_menu where menu_id = 2162;

insert into sys_menu values('2162', '钱包调账', '2007', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:wallet:adjust', '#', 'admin', sysdate(), '', null, '后台加减会员余额并记流水');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 2162
from sys_role_menu rm
where rm.menu_id in (2001, 2007, 2161)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 2162);
