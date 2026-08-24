SET NAMES utf8mb4;
-- 运营内容目录：把展示类菜单从业务管理/系统管理挪过来（可重复执行）
delete from sys_role_menu where menu_id = 2024;
delete from sys_menu where menu_id = 2024;
insert into sys_menu values('2024', '运营内容', '0', '7', 'content', null, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, 'App展示与运营内容目录');

-- 客服中心、视频轮播、新闻资讯、官方群聊、关于我们、运行概览、通知公告
update sys_menu set parent_id = 2024, order_num = 1 where menu_id = 2022;
update sys_menu set parent_id = 2024, order_num = 2 where menu_id = 2018;
update sys_menu set parent_id = 2024, order_num = 3 where menu_id = 2016;
update sys_menu set parent_id = 2024, order_num = 4 where menu_id = 2015;
update sys_menu set parent_id = 2024, order_num = 5 where menu_id = 2014;
update sys_menu set parent_id = 2024, order_num = 6 where menu_id = 2013;
update sys_menu set parent_id = 2024, order_num = 7 where menu_id = 107;

insert into sys_role_menu (role_id, menu_id)
select distinct role_id, 2024 from sys_role_menu
where menu_id in (107, 2013, 2014, 2015, 2016, 2018, 2022);
