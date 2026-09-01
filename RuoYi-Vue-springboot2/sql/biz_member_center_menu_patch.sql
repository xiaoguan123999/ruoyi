SET NAMES utf8mb4;
-- 会员中心目录（全新安装补齐 menu_id=2297；可重复执行）
-- 实名认证奖励、注册推广规则都挂在这个目录下。

insert into sys_menu
select '2297', '会员中心', '0', '5', 'memberCenter', null, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '会员、等级、团队与黑名单'
from dual where not exists (select 1 from sys_menu where menu_id = 2297);

update sys_menu set parent_id = 2297, order_num = 1 where menu_id = 2001;
update sys_menu set parent_id = 2297, order_num = 2 where menu_id = 2034;
update sys_menu set parent_id = 2297, order_num = 3 where menu_id = 2008;
update sys_menu set parent_id = 2297, order_num = 4 where menu_id = 2009;
update sys_menu set parent_id = 2297, order_num = 5 where menu_id = 2026;
update sys_menu set parent_id = 2297, order_num = 6 where menu_id = 2027;
update sys_menu set parent_id = 2297, order_num = 7 where menu_id = 2023;
update sys_menu set parent_id = 2297, order_num = 8 where menu_id = 2035;

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, 2297
from sys_role_menu rm
where rm.menu_id in (2001, 2008, 2009)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = 2297);

insert ignore into sys_role_menu values (1, 2297);

-- 子菜单都迁走后隐藏空的「业务管理」目录
update sys_menu
   set visible = '1'
 where menu_id = 2000
   and not exists (
     select 1 from (select menu_id from sys_menu where parent_id = 2000) t
   );
