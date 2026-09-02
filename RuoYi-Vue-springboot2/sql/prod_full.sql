-- =============================================================================
-- Fresh-server one-shot import. DROPs RuoYi/biz tables. Do NOT run on live data.
-- Database name ry-vue must match application-prod.yml JDBC url.
--
-- CLI:
--   mysql -uroot -p --default-character-set=utf8mb4 < sql/prod_full.sql
-- BT Panel / Navicat: import this file as a whole (do not execute statement-by-statement).
-- Contains DELIMITER procedures; if phpMyAdmin fails, use the mysql CLI above.
--
-- Default admin: admin / admin123    change password after first login.
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ry-vue`;


-- ---------- ry_20260417.sql ----------
-- ----------------------------
-- 1、部门表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment '部门id',
  parent_id         bigint(20)      default 0                  comment '父部门id',
  ancestors         varchar(50)     default ''                 comment '祖级列表',
  dept_name         varchar(30)     default ''                 comment '部门名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  leader            varchar(20)     default null               comment '负责人',
  phone             varchar(11)     default null               comment '联系电话',
  email             varchar(50)     default null               comment '邮箱',
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          '若依科技',   0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103,  101, '0,100,101',  '研发部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(104,  101, '0,100,101',  '市场部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(105,  101, '0,100,101',  '测试部门',   3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(106,  101, '0,100,101',  '财务部门',   4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(107,  101, '0,100,101',  '运维部门',   5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(108,  102, '0,100,102',  '市场部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(109,  102, '0,100,102',  '财务部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);


-- ----------------------------
-- 2、用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment '用户ID',
  dept_id           bigint(20)      default null               comment '部门ID',
  user_name         varchar(30)     not null                   comment '用户账号',
  nick_name         varchar(30)     not null                   comment '用户昵称',
  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
  email             varchar(50)     default ''                 comment '用户邮箱',
  phonenumber       varchar(11)     default ''                 comment '手机号码',
  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
  avatar            varchar(100)    default ''                 comment '头像地址',
  password          varchar(100)    default ''                 comment '密码',
  status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  login_ip          varchar(128)    default ''                 comment '最后登录IP',
  login_date        datetime                                   comment '最后登录时间',
  pwd_update_date   datetime                                   comment '密码最后更新时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1,  103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  105, 'ry',    '若依', '00', 'ry@qq.com',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试员');


-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment '岗位ID',
  post_code     varchar(64)     not null                   comment '岗位编码',
  post_name     varchar(50)     not null                   comment '岗位名称',
  post_sort     int(4)          not null                   comment '显示顺序',
  status        char(1)         not null                   comment '状态（0正常 1停用）',
  create_by     varchar(64)     default ''                 comment '创建者',
  create_time   datetime                                   comment '创建时间',
  update_by     varchar(64)     default ''			       comment '更新者',
  update_time   datetime                                   comment '更新时间',
  remark        varchar(500)    default null               comment '备注',
  primary key (post_id)
) engine=innodb comment = '岗位信息表';

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------
insert into sys_post values(1, 'ceo',  '董事长',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   '项目经理',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   '人力资源',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', '普通员工',  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment '角色ID',
  role_name            varchar(30)     not null                   comment '角色名称',
  role_key             varchar(100)    not null                   comment '角色权限字符串',
  role_sort            int(4)          not null                   comment '显示顺序',
  data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
  dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
  status               char(1)         not null                   comment '角色状态（0正常 1停用）',
  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------
insert into sys_role values('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');


-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
  menu_name         varchar(50)     not null                   comment '菜单名称',
  parent_id         bigint(20)      default 0                  comment '父菜单ID',
  order_num         int(4)          default 0                  comment '显示顺序',
  path              varchar(200)    default ''                 comment '路由地址',
  component         varchar(255)    default null               comment '组件路径',
  query             varchar(255)    default null               comment '路由参数',
  route_name        varchar(50)     default ''                 comment '路由名称',
  is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
  is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
  menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
  visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
  status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
  perms             varchar(100)    default null               comment '权限标识',
  icon              varchar(100)    default '#'                comment '菜单图标',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
insert into sys_menu values('1', '系统管理', '0', '1', 'system',           null, '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', sysdate(), '', null, '系统管理目录');
insert into sys_menu values('2', '系统监控', '0', '2', 'monitor',          null, '', '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', sysdate(), '', null, '系统监控目录');
insert into sys_menu values('3', '系统工具', '0', '3', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, '系统工具目录');
insert into sys_menu values('4', '若依官网', '0', '4', 'http://ruoyi.vip', null, '', '', 0, 0, 'M', '0', '0', '', 'guide',    'admin', sysdate(), '', null, '若依官网地址');
-- 二级菜单
insert into sys_menu values('100',  '用户管理', '1',   '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', sysdate(), '', null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理', '1',   '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', sysdate(), '', null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理', '1',   '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', sysdate(), '', null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '部门管理菜单');
insert into sys_menu values('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, '岗位管理菜单');
insert into sys_menu values('105',  '字典管理', '1',   '6', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', sysdate(), '', null, '字典管理菜单');
insert into sys_menu values('106',  '参数设置', '1',   '7', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', sysdate(), '', null, '参数设置菜单');
insert into sys_menu values('107',  '通知公告', '1',   '8', 'notice',     'system/notice/index',      '', '', 1, 0, 'C', '0', '0', 'system:notice:list',      'message',       'admin', sysdate(), '', null, '通知公告菜单');
insert into sys_menu values('108',  '日志管理', '1',   '9', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', sysdate(), '', null, '日志管理菜单');
insert into sys_menu values('109',  '在线用户', '2',   '1', 'online',     'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',     'online',        'admin', sysdate(), '', null, '在线用户菜单');
insert into sys_menu values('110',  '定时任务', '2',   '2', 'job',        'monitor/job/index',        '', '', 1, 0, 'C', '0', '0', 'monitor:job:list',        'job',           'admin', sysdate(), '', null, '定时任务菜单');
insert into sys_menu values('111',  '数据监控', '2',   '3', 'druid',      'monitor/druid/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',      'druid',         'admin', sysdate(), '', null, '数据监控菜单');
insert into sys_menu values('112',  '服务监控', '2',   '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, '服务监控菜单');
insert into sys_menu values('113',  '缓存监控', '2',   '5', 'cache',      'monitor/cache/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis',         'admin', sysdate(), '', null, '缓存监控菜单');
insert into sys_menu values('114',  '缓存列表', '2',   '6', 'cacheList',  'monitor/cache/list',       '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis-list',    'admin', sysdate(), '', null, '缓存列表菜单');
insert into sys_menu values('115',  '表单构建', '3',   '1', 'build',      'tool/build/index',         '', '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, '表单构建菜单');
insert into sys_menu values('116',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           '', '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, '代码生成菜单');
insert into sys_menu values('117',  '系统接口', '3',   '3', 'swagger',    'tool/swagger/index',       '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',       'swagger',       'admin', sysdate(), '', null, '系统接口菜单');
-- 三级菜单
insert into sys_menu values('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          'admin', sysdate(), '', null, '操作日志菜单');
insert into sys_menu values('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    'admin', sysdate(), '', null, '登录日志菜单');
-- 用户管理按钮
insert into sys_menu values('1000', '用户查询', '100', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1001', '用户新增', '100', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', '用户修改', '100', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', '用户删除', '100', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', '用户导出', '100', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', '用户导入', '100', '6',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', '重置密码', '100', '7',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- 角色管理按钮
insert into sys_menu values('1007', '角色查询', '101', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1008', '角色新增', '101', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', '角色修改', '101', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', '角色删除', '101', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', '角色导出', '101', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- 菜单管理按钮
insert into sys_menu values('1012', '菜单查询', '102', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1013', '菜单新增', '102', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', '菜单修改', '102', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', '菜单删除', '102', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- 部门管理按钮
insert into sys_menu values('1016', '部门查询', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1017', '部门新增', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', '部门修改', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', '部门删除', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- 岗位管理按钮
insert into sys_menu values('1020', '岗位查询', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1021', '岗位新增', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', '岗位修改', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', '岗位删除', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', '岗位导出', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- 字典管理按钮
insert into sys_menu values('1025', '字典查询', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1026', '字典新增', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', '字典修改', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', '字典删除', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', '字典导出', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- 参数设置按钮
insert into sys_menu values('1030', '参数查询', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1031', '参数新增', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', '参数修改', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', '参数删除', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', '参数导出', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- 通知公告按钮
insert into sys_menu values('1035', '公告查询', '107', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1036', '公告新增', '107', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', '公告修改', '107', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', '公告删除', '107', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- 操作日志按钮
insert into sys_menu values('1039', '操作查询', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1040', '操作删除', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', '日志导出', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- 登录日志按钮
insert into sys_menu values('1042', '登录查询', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1043', '登录删除', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', '日志导出', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', '账户解锁', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', sysdate(), '', null, '');
-- 在线用户按钮
insert into sys_menu values('1046', '在线查询', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', '批量强退', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', '单条强退', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- 定时任务按钮
insert into sys_menu values('1049', '任务查询', '110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', '任务新增', '110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', '任务修改', '110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', '任务删除', '110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', '状态修改', '110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', '任务导出', '110', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- 代码生成按钮
insert into sys_menu values('1055', '生成查询', '116', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', '生成修改', '116', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', '生成删除', '116', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', '导入代码', '116', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', '预览代码', '116', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', '生成代码', '116', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment '用户ID',
  role_id   bigint(20) not null comment '角色ID',
  primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment '角色ID',
  menu_id   bigint(20) not null comment '菜单ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------
insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '3');
insert into sys_role_menu values ('2', '4');
insert into sys_role_menu values ('2', '100');
insert into sys_role_menu values ('2', '101');
insert into sys_role_menu values ('2', '102');
insert into sys_role_menu values ('2', '103');
insert into sys_role_menu values ('2', '104');
insert into sys_role_menu values ('2', '105');
insert into sys_role_menu values ('2', '106');
insert into sys_role_menu values ('2', '107');
insert into sys_role_menu values ('2', '108');
insert into sys_role_menu values ('2', '109');
insert into sys_role_menu values ('2', '110');
insert into sys_role_menu values ('2', '111');
insert into sys_role_menu values ('2', '112');
insert into sys_role_menu values ('2', '113');
insert into sys_role_menu values ('2', '114');
insert into sys_role_menu values ('2', '115');
insert into sys_role_menu values ('2', '116');
insert into sys_role_menu values ('2', '117');
insert into sys_role_menu values ('2', '500');
insert into sys_role_menu values ('2', '501');
insert into sys_role_menu values ('2', '1000');
insert into sys_role_menu values ('2', '1001');
insert into sys_role_menu values ('2', '1002');
insert into sys_role_menu values ('2', '1003');
insert into sys_role_menu values ('2', '1004');
insert into sys_role_menu values ('2', '1005');
insert into sys_role_menu values ('2', '1006');
insert into sys_role_menu values ('2', '1007');
insert into sys_role_menu values ('2', '1008');
insert into sys_role_menu values ('2', '1009');
insert into sys_role_menu values ('2', '1010');
insert into sys_role_menu values ('2', '1011');
insert into sys_role_menu values ('2', '1012');
insert into sys_role_menu values ('2', '1013');
insert into sys_role_menu values ('2', '1014');
insert into sys_role_menu values ('2', '1015');
insert into sys_role_menu values ('2', '1016');
insert into sys_role_menu values ('2', '1017');
insert into sys_role_menu values ('2', '1018');
insert into sys_role_menu values ('2', '1019');
insert into sys_role_menu values ('2', '1020');
insert into sys_role_menu values ('2', '1021');
insert into sys_role_menu values ('2', '1022');
insert into sys_role_menu values ('2', '1023');
insert into sys_role_menu values ('2', '1024');
insert into sys_role_menu values ('2', '1025');
insert into sys_role_menu values ('2', '1026');
insert into sys_role_menu values ('2', '1027');
insert into sys_role_menu values ('2', '1028');
insert into sys_role_menu values ('2', '1029');
insert into sys_role_menu values ('2', '1030');
insert into sys_role_menu values ('2', '1031');
insert into sys_role_menu values ('2', '1032');
insert into sys_role_menu values ('2', '1033');
insert into sys_role_menu values ('2', '1034');
insert into sys_role_menu values ('2', '1035');
insert into sys_role_menu values ('2', '1036');
insert into sys_role_menu values ('2', '1037');
insert into sys_role_menu values ('2', '1038');
insert into sys_role_menu values ('2', '1039');
insert into sys_role_menu values ('2', '1040');
insert into sys_role_menu values ('2', '1041');
insert into sys_role_menu values ('2', '1042');
insert into sys_role_menu values ('2', '1043');
insert into sys_role_menu values ('2', '1044');
insert into sys_role_menu values ('2', '1045');
insert into sys_role_menu values ('2', '1046');
insert into sys_role_menu values ('2', '1047');
insert into sys_role_menu values ('2', '1048');
insert into sys_role_menu values ('2', '1049');
insert into sys_role_menu values ('2', '1050');
insert into sys_role_menu values ('2', '1051');
insert into sys_role_menu values ('2', '1052');
insert into sys_role_menu values ('2', '1053');
insert into sys_role_menu values ('2', '1054');
insert into sys_role_menu values ('2', '1055');
insert into sys_role_menu values ('2', '1056');
insert into sys_role_menu values ('2', '1057');
insert into sys_role_menu values ('2', '1058');
insert into sys_role_menu values ('2', '1059');
insert into sys_role_menu values ('2', '1060');

-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment '角色ID',
  dept_id   bigint(20) not null comment '部门ID',
  primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------
insert into sys_role_dept values ('2', '100');
insert into sys_role_dept values ('2', '101');
insert into sys_role_dept values ('2', '105');


-- ----------------------------
-- 9、用户与岗位关联表  用户1-N岗位
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment '用户ID',
  post_id   bigint(20) not null comment '岗位ID',
  primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- ----------------------------
-- 初始化-用户与岗位关联表数据
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');


-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
  method            varchar(200)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  dept_name         varchar(50)     default ''                 comment '部门名称',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(128)    default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  cost_time         bigint(20)      default 0                  comment '消耗时间',
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';


-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment '字典主键',
  dict_name        varchar(100)    default ''                 comment '字典名称',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

insert into sys_dict_type values(1,  '用户性别', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '用户性别列表');
insert into sys_dict_type values(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '菜单状态列表');
insert into sys_dict_type values(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '系统开关列表');
insert into sys_dict_type values(4,  '任务状态', 'sys_job_status',      '0', 'admin', sysdate(), '', null, '任务状态列表');
insert into sys_dict_type values(5,  '任务分组', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '任务分组列表');
insert into sys_dict_type values(6,  '系统是否', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '系统是否列表');
insert into sys_dict_type values(7,  '通知类型', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, '通知类型列表');
insert into sys_dict_type values(8,  '通知状态', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, '通知状态列表');
insert into sys_dict_type values(9,  '操作类型', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '操作类型列表');
insert into sys_dict_type values(10, '系统状态', 'sys_common_status',   '0', 'admin', sysdate(), '', null, '登录状态列表');


-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment '字典编码',
  dict_sort        int(4)          default 0                  comment '字典排序',
  dict_label       varchar(100)    default ''                 comment '字典标签',
  dict_value       varchar(100)    default ''                 comment '字典键值',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
  list_class       varchar(100)    default null               comment '表格回显样式',
  is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典数据表';

insert into sys_dict_data values(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '性别男');
insert into sys_dict_data values(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别女');
insert into sys_dict_data values(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别未知');
insert into sys_dict_data values(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '显示菜单');
insert into sys_dict_data values(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '隐藏菜单');
insert into sys_dict_data values(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(8,  1,  '正常',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(9,  2,  '暂停',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(10, 1,  '默认',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '默认分组');
insert into sys_dict_data values(11, 2,  '系统',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '系统分组');
insert into sys_dict_data values(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统默认是');
insert into sys_dict_data values(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '系统默认否');
insert into sys_dict_data values(14, 1,  '通知',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, '通知');
insert into sys_dict_data values(15, 2,  '公告',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, '公告');
insert into sys_dict_data values(16, 1,  '正常',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(17, 2,  '关闭',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '关闭状态');
insert into sys_dict_data values(18, 99, '其他',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '其他操作');
insert into sys_dict_data values(19, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data values(20, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data values(21, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '删除操作');
insert into sys_dict_data values(22, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '授权操作');
insert into sys_dict_data values(23, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导出操作');
insert into sys_dict_data values(24, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导入操作');
insert into sys_dict_data values(25, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '强退操作');
insert into sys_dict_data values(26, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '生成操作');
insert into sys_dict_data values(27, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '清空操作');
insert into sys_dict_data values(28, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(29, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');


-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment '参数主键',
  config_name       varchar(100)    default ''                 comment '参数名称',
  config_key        varchar(100)    default ''                 comment '参数键名',
  config_value      varchar(500)    default ''                 comment '参数键值',
  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

insert into sys_config values(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', sysdate(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',        'Y', 'admin', sysdate(), '', null, '初始化密码 123456' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'true',          'Y', 'admin', sysdate(), '', null, '是否开启验证码功能（true开启，false关闭）');
insert into sys_config values(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, '是否开启注册用户功能（true开启，false关闭）');
insert into sys_config values(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',              'Y', 'admin', sysdate(), '', null, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
insert into sys_config values(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', sysdate(), '', null, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
insert into sys_config values(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', sysdate(), '', null, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
insert into sys_config values(9, '用户管理-密码字符范围',         'sys.account.chrtype',              '0',             'Y', 'admin', sysdate(), '', null, '默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');


-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  user_name      varchar(50)    default ''                comment '用户账号',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系统访问记录';


-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment '任务ID',
  job_name            varchar(64)   default ''                 comment '任务名称',
  job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
  invoke_target       varchar(500)  not null                   comment '调用目标字符串',
  cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
  misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
  status              char(1)       default '0'                comment '状态（0正常 1暂停）',
  create_by           varchar(64)   default ''                 comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)   default ''                 comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)  default ''                 comment '备注信息',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

insert into sys_job values(1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
  job_name            varchar(64)    not null                   comment '任务名称',
  job_group           varchar(64)    not null                   comment '任务组名',
  invoke_target       varchar(500)   not null                   comment '调用目标字符串',
  job_message         varchar(500)                              comment '日志信息',
  status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
  exception_info      varchar(2000)  default ''                 comment '异常信息',
  start_time          datetime                                  comment '执行开始时间',
  end_time            datetime                                  comment '执行结束时间',
  create_time         datetime                                  comment '创建时间',
  primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';


-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment '公告ID',
  notice_title      varchar(50)     not null                   comment '公告标题',
  notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',
  notice_content    longblob        default null               comment '公告内容',
  status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(255)    default null               comment '备注',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 初始化-公告信息表数据
-- ----------------------------
insert into sys_notice values('1', '温馨提醒：2018-07-01 若依新版本发布啦', '2', '新版本内容', '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('2', '维护通知：2018-07-01 若依系统凌晨维护', '1', '维护内容',   '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('3', '若依开源框架介绍', '1', '<p><span style=\"color: rgb(230, 0, 0);\">项目介绍</span></p><p><font color=\"#333333\">RuoYi开源项目是为企业用户定制的后台脚手架框架，为企业打造的一站式解决方案，降低企业开发成本，提升开发效率。主要包括用户管理、角色管理、部门管理、菜单管理、参数管理、字典管理、</font><span style=\"color: rgb(51, 51, 51);\">岗位管理</span><span style=\"color: rgb(51, 51, 51);\">、定时任务</span><span style=\"color: rgb(51, 51, 51);\">、</span><span style=\"color: rgb(51, 51, 51);\">服务监控、登录日志、操作日志、代码生成等功能。其中，还支持多数据源、数据权限、国际化、Redis缓存、Docker部署、滑动验证码、第三方认证登录、分布式事务、</span><font color=\"#333333\">分布式文件存储</font><span style=\"color: rgb(51, 51, 51);\">、分库分表处理等技术特点。</span></p><p><img src=\"https://foruda.gitee.com/images/1773931848342439032/a4d22313_1815095.png\" style=\"width: 64px;\"><br></p><p><span style=\"color: rgb(230, 0, 0);\">官网及演示</span></p><p><span style=\"color: rgb(51, 51, 51);\">若依官网地址：&nbsp;</span><a href=\"http://ruoyi.vip\" target=\"_blank\">http://ruoyi.vip</a><a href=\"http://ruoyi.vip\" target=\"_blank\"></a></p><p><span style=\"color: rgb(51, 51, 51);\">若依文档地址：&nbsp;</span><a href=\"http://doc.ruoyi.vip\" target=\"_blank\">http://doc.ruoyi.vip</a><br></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【不分离版】：&nbsp;</span><a href=\"http://demo.ruoyi.vip\" target=\"_blank\">http://demo.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【分离版本】：&nbsp;</span><a href=\"http://vue.ruoyi.vip\" target=\"_blank\">http://vue.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【微服务版】：&nbsp;</span><a href=\"http://cloud.ruoyi.vip\" target=\"_blank\">http://cloud.ruoyi.vip</a></p><p><span style=\"color: rgb(51, 51, 51);\">演示地址【移动端版】：&nbsp;</span><a href=\"http://h5.ruoyi.vip\" target=\"_blank\">http://h5.ruoyi.vip</a></p><p><br style=\"color: rgb(48, 49, 51); font-family: &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 12px;\"></p>', '0', 'admin', sysdate(), '', null, '管理员');


-- ----------------------------
-- 18、公告已读记录表
-- ----------------------------
drop table if exists sys_notice_read;
create table sys_notice_read (
  read_id          bigint(20)       not null auto_increment    comment '已读主键',
  notice_id        int(4)           not null                   comment '公告id',
  user_id          bigint(20)       not null                   comment '用户id',
  read_time        datetime         not null                   comment '阅读时间',
  primary key (read_id),
  unique key uk_user_notice (user_id, notice_id)   comment '同一用户同一公告只记录一次'
) engine=innodb auto_increment=1 comment='公告已读记录表';


-- ----------------------------
-- 19、代码生成业务表
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment '编号',
  table_name        varchar(200)    default ''                 comment '表名称',
  table_comment     varchar(500)    default ''                 comment '表描述',
  sub_table_name    varchar(64)     default null               comment '关联子表的表名',
  sub_table_fk_name varchar(64)     default null               comment '子表关联的外键名',
  class_name        varchar(100)    default ''                 comment '实体类名称',
  tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',
  tpl_web_type      varchar(30)     default ''                 comment '前端模板类型（element-ui模版 element-plus模版）',
  package_name      varchar(100)                               comment '生成包路径',
  module_name       varchar(30)                                comment '生成模块名',
  business_name     varchar(30)                                comment '生成业务名',
  function_name     varchar(50)                                comment '生成功能名',
  function_author   varchar(50)                                comment '生成功能作者',
  form_col_num      int(1)          default 1                  comment '表单布局（单列 双列 三列）',
  gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',
  gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',
  options           varchar(1000)                              comment '其它生成选项',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表';


-- ----------------------------
-- 20、代码生成业务表字段
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment '编号',
  table_id          bigint(20)                                 comment '归属表编号',
  column_name       varchar(200)                               comment '列名称',
  column_comment    varchar(500)                               comment '列描述',
  column_type       varchar(100)                               comment '列类型',
  java_type         varchar(500)                               comment 'JAVA类型',
  java_field        varchar(200)                               comment 'JAVA字段名',
  is_pk             char(1)                                    comment '是否主键（1是）',
  is_increment      char(1)                                    comment '是否自增（1是）',
  is_required       char(1)                                    comment '是否必填（1是）',
  is_insert         char(1)                                    comment '是否为插入字段（1是）',
  is_edit           char(1)                                    comment '是否编辑字段（1是）',
  is_list           char(1)                                    comment '是否列表字段（1是）',
  is_query          char(1)                                    comment '是否查询字段（1是）',
  query_type        varchar(200)    default 'EQ'               comment '查询方式（等于、不等于、大于、小于、范围）',
  html_type         varchar(200)                               comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  dict_type         varchar(200)    default ''                 comment '字典类型',
  sort              int                                        comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';

-- ---------- quartz.sql ----------
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

-- ----------------------------
-- 1、存储每一个已配置的 jobDetail 的详细信息
-- ----------------------------
create table QRTZ_JOB_DETAILS (
    sched_name           varchar(120)    not null            comment '调度名称',
    job_name             varchar(200)    not null            comment '任务名称',
    job_group            varchar(200)    not null            comment '任务组名',
    description          varchar(250)    null                comment '相关介绍',
    job_class_name       varchar(250)    not null            comment '执行任务类名称',
    is_durable           varchar(1)      not null            comment '是否持久化',
    is_nonconcurrent     varchar(1)      not null            comment '是否并发',
    is_update_data       varchar(1)      not null            comment '是否更新数据',
    requests_recovery    varchar(1)      not null            comment '是否接受恢复执行',
    job_data             blob            null                comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
) engine=innodb comment = '任务详细信息表';

-- ----------------------------
-- 2、 存储已配置的 Trigger 的信息
-- ----------------------------
create table QRTZ_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment '触发器的名字',
    trigger_group        varchar(200)    not null            comment '触发器所属组的名字',
    job_name             varchar(200)    not null            comment 'qrtz_job_details表job_name的外键',
    job_group            varchar(200)    not null            comment 'qrtz_job_details表job_group的外键',
    description          varchar(250)    null                comment '相关介绍',
    next_fire_time       bigint(13)      null                comment '上一次触发时间（毫秒）',
    prev_fire_time       bigint(13)      null                comment '下一次触发时间（默认为-1表示不触发）',
    priority             integer         null                comment '优先级',
    trigger_state        varchar(16)     not null            comment '触发器状态',
    trigger_type         varchar(8)      not null            comment '触发器的类型',
    start_time           bigint(13)      not null            comment '开始时间',
    end_time             bigint(13)      null                comment '结束时间',
    calendar_name        varchar(200)    null                comment '日程表名称',
    misfire_instr        smallint(2)     null                comment '补偿执行的策略',
    job_data             blob            null                comment '存放持久化job对象',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS(sched_name, job_name, job_group)
) engine=innodb comment = '触发器详细信息表';

-- ----------------------------
-- 3、 存储简单的 Trigger，包括重复次数，间隔，以及已触发的次数
-- ----------------------------
create table QRTZ_SIMPLE_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    repeat_count         bigint(7)       not null            comment '重复的次数统计',
    repeat_interval      bigint(12)      not null            comment '重复的间隔时间',
    times_triggered      bigint(10)      not null            comment '已经触发的次数',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = '简单触发器的信息表';

-- ----------------------------
-- 4、 存储 Cron Trigger，包括 Cron 表达式和时区信息
-- ---------------------------- 
create table QRTZ_CRON_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    cron_expression      varchar(200)    not null            comment 'cron表达式',
    time_zone_id         varchar(80)                         comment '时区',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Cron类型的触发器表';

-- ----------------------------
-- 5、 Trigger 作为 Blob 类型存储(用于 Quartz 用户用 JDBC 创建他们自己定制的 Trigger 类型，JobStore 并不知道如何存储实例的时候)
-- ---------------------------- 
create table QRTZ_BLOB_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    blob_data            blob            null                comment '存放持久化Trigger对象',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Blob类型的触发器表';

-- ----------------------------
-- 6、 以 Blob 类型存储存放日历信息， quartz可配置一个日历来指定一个时间范围
-- ---------------------------- 
create table QRTZ_CALENDARS (
    sched_name           varchar(120)    not null            comment '调度名称',
    calendar_name        varchar(200)    not null            comment '日历名称',
    calendar             blob            not null            comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
) engine=innodb comment = '日历信息表';

-- ----------------------------
-- 7、 存储已暂停的 Trigger 组的信息
-- ---------------------------- 
create table QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
) engine=innodb comment = '暂停的触发器表';

-- ----------------------------
-- 8、 存储与已触发的 Trigger 相关的状态信息，以及相联 Job 的执行信息
-- ---------------------------- 
create table QRTZ_FIRED_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    entry_id             varchar(95)     not null            comment '调度器实例id',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    instance_name        varchar(200)    not null            comment '调度器实例名',
    fired_time           bigint(13)      not null            comment '触发的时间',
    sched_time           bigint(13)      not null            comment '定时器制定的时间',
    priority             integer         not null            comment '优先级',
    state                varchar(16)     not null            comment '状态',
    job_name             varchar(200)    null                comment '任务名称',
    job_group            varchar(200)    null                comment '任务组名',
    is_nonconcurrent     varchar(1)      null                comment '是否并发',
    requests_recovery    varchar(1)      null                comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
) engine=innodb comment = '已触发的触发器表';

-- ----------------------------
-- 9、 存储少量的有关 Scheduler 的状态信息，假如是用于集群中，可以看到其他的 Scheduler 实例
-- ---------------------------- 
create table QRTZ_SCHEDULER_STATE (
    sched_name           varchar(120)    not null            comment '调度名称',
    instance_name        varchar(200)    not null            comment '实例名称',
    last_checkin_time    bigint(13)      not null            comment '上次检查时间',
    checkin_interval     bigint(13)      not null            comment '检查间隔时间',
    primary key (sched_name, instance_name)
) engine=innodb comment = '调度器状态表';

-- ----------------------------
-- 10、 存储程序的悲观锁的信息(假如使用了悲观锁)
-- ---------------------------- 
create table QRTZ_LOCKS (
    sched_name           varchar(120)    not null            comment '调度名称',
    lock_name            varchar(40)     not null            comment '悲观锁名称',
    primary key (sched_name, lock_name)
) engine=innodb comment = '存储的悲观锁信息表';

-- ----------------------------
-- 11、 Quartz集群实现同步机制的行锁表
-- ---------------------------- 
create table QRTZ_SIMPROP_TRIGGERS (
    sched_name           varchar(120)    not null            comment '调度名称',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggers表trigger_name的外键',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggers表trigger_group的外键',
    str_prop_1           varchar(512)    null                comment 'String类型的trigger的第一个参数',
    str_prop_2           varchar(512)    null                comment 'String类型的trigger的第二个参数',
    str_prop_3           varchar(512)    null                comment 'String类型的trigger的第三个参数',
    int_prop_1           int             null                comment 'int类型的trigger的第一个参数',
    int_prop_2           int             null                comment 'int类型的trigger的第二个参数',
    long_prop_1          bigint          null                comment 'long类型的trigger的第一个参数',
    long_prop_2          bigint          null                comment 'long类型的trigger的第二个参数',
    dec_prop_1           numeric(13,4)   null                comment 'decimal类型的trigger的第一个参数',
    dec_prop_2           numeric(13,4)   null                comment 'decimal类型的trigger的第二个参数',
    bool_prop_1          varchar(1)      null                comment 'Boolean类型的trigger的第一个参数',
    bool_prop_2          varchar(1)      null                comment 'Boolean类型的trigger的第二个参数',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = '同步机制的行锁表';

commit;

-- ---------- biz_init.sql ----------
-- ----------------------------
-- 简易认购返利业务初始化
-- ----------------------------

-- 会员
drop table if exists biz_member;
create table biz_member (
  member_id         bigint(20)      not null auto_increment    comment '会员ID/邀请码',
  phone             varchar(20)     not null                   comment '手机号',
  password          varchar(100)    not null                   comment '密码',
  pay_password      varchar(100)    default ''                 comment '支付/交易密码',
  invite_code       varchar(32)     default ''                 comment '邀请码(7位随机数字)',
  parent_id         bigint(20)      default null               comment '上级会员ID',
  ancestors         varchar(500)    default '0'                comment '祖级列表',
  real_name         varchar(50)     default ''                 comment '真实姓名',
  id_card           varchar(32)     default ''                 comment '身份证号',
  kyc_status        char(1)         default '0'                comment '实名状态（0未实名 1已实名）',
  level_id          bigint(20)      default null               comment '会员等级ID，空表示无等级',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  ga_secret         varchar(64)     default ''                 comment '谷歌验证密钥',
  ga_status         char(1)         default '0'                comment '谷歌验证（0未绑定 1已绑定）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (member_id),
  unique key uk_biz_member_phone (phone),
  unique key uk_biz_member_invite (invite_code),
  key idx_biz_member_parent (parent_id)
) engine=innodb auto_increment=10001 comment = 'C端会员';

-- 钱包（CNY / USDT 独立）
drop table if exists biz_wallet;
create table biz_wallet (
  wallet_id         bigint(20)      not null auto_increment    comment '钱包ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种 CNY/USDT',
  available         decimal(18,4)   default 0                  comment '可用余额',
  frozen            decimal(18,4)   default 0                  comment '冻结余额',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (wallet_id),
  unique key uk_biz_wallet_member_currency (member_id, currency)
) engine=innodb comment = '会员钱包';

-- 资金流水
drop table if exists biz_wallet_log;
create table biz_wallet_log (
  log_id            bigint(20)      not null auto_increment    comment '流水ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种',
  biz_type          varchar(32)     not null                   comment '业务类型',
  biz_id            bigint(20)      default null               comment '业务单号',
  amount            decimal(18,4)   not null                   comment '变动金额',
  available_before  decimal(18,4)   default 0                  comment '变动前可用',
  available_after   decimal(18,4)   default 0                  comment '变动后可用',
  frozen_before     decimal(18,4)   default 0                  comment '变动前冻结',
  frozen_after      decimal(18,4)   default 0                  comment '变动后冻结',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '创建时间',
  primary key (log_id),
  key idx_biz_wallet_log_member (member_id, create_time)
) engine=innodb comment = '资金流水';

-- 签到
drop table if exists biz_checkin;
create table biz_checkin (
  checkin_id        bigint(20)      not null auto_increment    comment '签到ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  checkin_date      date            not null                   comment '签到日期',
  amount            decimal(18,4)   not null                   comment '奖励金额',
  currency          varchar(16)     default 'CNY'              comment '币种',
  create_time       datetime                                   comment '创建时间',
  primary key (checkin_id),
  unique key uk_biz_checkin_member_date (member_id, checkin_date)
) engine=innodb comment = '每日签到';

-- 签到连续抽奖
drop table if exists biz_checkin_prize;
create table biz_checkin_prize (
  prize_log_id      bigint(20)      not null auto_increment    comment '抽奖记录ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  checkin_id        bigint(20)      not null                   comment '签到ID',
  streak_days       int(11)         not null                   comment '连续签到天数',
  prize_name        varchar(100)    not null                   comment '奖品名称',
  won               char(1)         not null default '0'       comment '是否中奖（0未中 1已中）',
  create_time       datetime                                   comment '创建时间',
  primary key (prize_log_id),
  unique key uk_biz_checkin_prize_once (member_id, checkin_id, streak_days),
  key idx_biz_checkin_prize_member (member_id, won)
) engine=innodb comment = '签到连续抽奖记录';



-- 产品
drop table if exists biz_product;
create table biz_product (
  product_id        bigint(20)      not null auto_increment    comment '产品ID',
  product_name      varchar(100)    not null                   comment '产品名称',
  currency          varchar(16)     not null default 'CNY'     comment 'CNY/USDT',
  price             decimal(18,4)   not null                   comment '认购价格(CNY)',
  daily_rebate      decimal(18,4)   not null                   comment '每日返利(CNY)',
  duration_days     int(11)         not null                   comment '返利天数',
  withdraw_required char(1)         default '0'                comment '是否提现指定产品（0否 1是）',
  status            char(1)         default '0'                comment '状态（0上架 1下架）',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (product_id)
) engine=innodb auto_increment=1 comment = '认购产品';

-- 认购订单
drop table if exists biz_order;
create table biz_order (
  order_id          bigint(20)      not null auto_increment    comment '订单ID',
  order_no          varchar(32)     not null                   comment '订单号',
  member_id         bigint(20)      not null                   comment '会员ID',
  product_id        bigint(20)      not null                   comment '产品ID',
  product_name      varchar(100)    default ''                 comment '产品名称快照',
  currency          varchar(16)     not null default 'CNY'     comment 'CNY/USDT',
  price             decimal(18,4)   not null                   comment '认购价格',
  quantity          int(11)         not null default 1         comment '认购份数',
  daily_rebate      decimal(18,4)   not null                   comment '每日返利',
  duration_days     int(11)         not null                   comment '总天数',
  remaining_days    int(11)         not null                   comment '剩余天数',
  last_rebate_date  date            default null               comment '上次返利日期',
  withdraw_required char(1)         default '0'                comment '是否提现指定产品',
  status            char(1)         default '0'                comment '状态（0持仓 1完成）',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (order_id),
  unique key uk_biz_order_no (order_no),
  key idx_biz_order_member (member_id, status)
) engine=innodb comment = '认购订单';

-- 返利记录
drop table if exists biz_rebate_log;
create table biz_rebate_log (
  rebate_id         bigint(20)      not null auto_increment    comment '返利ID',
  order_id          bigint(20)      not null                   comment '订单ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null default 'CNY'     comment 'CNY/USDT',
  amount            decimal(18,4)   not null                   comment '返利金额',
  rebate_date       date            not null                   comment '返利日期',
  create_time       datetime                                   comment '创建时间',
  primary key (rebate_id),
  unique key uk_biz_rebate_order_date (order_id, rebate_date)
) engine=innodb comment = '产品返利记录';

-- 充值
drop table if exists biz_recharge;
create table biz_recharge (
  recharge_id       bigint(20)      not null auto_increment    comment '充值ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种',
  amount            decimal(18,4)   not null                   comment '金额',
  status            char(1)         default '0'                comment '状态（0待审 1通过 2拒绝）',
  audit_by          varchar(64)     default ''                 comment '审核人',
  audit_time        datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (recharge_id),
  key idx_biz_recharge_member (member_id, status)
) engine=innodb comment = '充值申请';

-- 提现
drop table if exists biz_withdraw;
create table biz_withdraw (
  withdraw_id       bigint(20)      not null auto_increment    comment '提现ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  currency          varchar(16)     not null                   comment '币种',
  amount            decimal(18,4)   not null                   comment '金额',
  fee_amount        decimal(18,4)   default 0                  comment '手续费',
  arrival_amount    decimal(18,4)   default 0                  comment '到账金额',
  account_info      varchar(255)    default ''                 comment '收款信息（占位）',
  pay_method        varchar(32)     default ''                 comment 'pay method ALIPAY/USDT',
  status            char(1)         default '0'                comment '状态（0待审 1通过 2拒绝）',
  audit_by          varchar(64)     default ''                 comment '审核人',
  audit_time        datetime                                   comment '审核时间',
  audit_remark      varchar(500)    default ''                 comment '审核备注',
  pay_proof_url     varchar(500)    default ''                 comment 'pay proof url',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (withdraw_id),
  key idx_biz_withdraw_member (member_id, status)
) engine=innodb comment = '提现申请';

-- 分佣
drop table if exists biz_commission_log;
create table biz_commission_log (
  commission_id     bigint(20)      not null auto_increment    comment '分佣ID',
  from_member_id    bigint(20)      not null                   comment '来源会员',
  to_member_id      bigint(20)      not null                   comment '获得分佣会员',
  team_level        int(4)          not null                   comment '层级 1/2/3',
  currency          varchar(16)     not null                   comment '币种',
  base_amount       decimal(18,4)   not null                   comment '认购本金',
  rate              decimal(10,4)   not null                   comment '比例',
  amount            decimal(18,4)   not null                   comment '分佣金额',
  recharge_id       bigint(20)      default null               comment '充值单ID（历史）',
  order_id          bigint(20)      default null               comment '认购订单ID',
  create_time       datetime                                   comment '创建时间',
  primary key (commission_id),
  key idx_biz_commission_to (to_member_id),
  key idx_biz_commission_from (from_member_id)
) engine=innodb comment = '团队分佣记录';

-- 会员等级
drop table if exists biz_level;
create table biz_level (
  level_id            bigint(20)      not null auto_increment  comment '等级ID',
  level_name          varchar(50)     not null                 comment '等级名称',
  min_valid_members   int(11)         default 0                comment '最低有效会员人数',
  min_recharge_cny    decimal(18,4)   default 0                comment '最低累计充值(CNY)',
  min_recharge_usdt   decimal(18,4)   default 0                comment '最低累计充值(USDT，预留)',
  sort                int(4)          default 0                comment '排序(越大越高)',
  status              char(1)         default '0'              comment '状态（0正常 1停用）',
  create_by           varchar(64)     default ''               comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)     default ''               comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)    default null             comment '备注',
  primary key (level_id)
) engine=innodb auto_increment=1 comment = '会员等级';

-- 种子：等级
insert into biz_level values(1, 'V0', 0, 0, 0, 0, '0', 'admin', sysdate(), '', null, '默认等级');
insert into biz_level values(2, 'V1', 3, 1000, 0, 1, '0', 'admin', sysdate(), '', null, '占位阈值，可后台调整');
insert into biz_level values(3, 'V2', 10, 5000, 0, 2, '0', 'admin', sysdate(), '', null, '占位阈值，可后台调整');
insert into biz_level values(4, 'V3', 30, 20000, 0, 3, '0', 'admin', sysdate(), '', null, '占位阈值，可后台调整');

-- 种子：提现指定产品
insert into biz_product values(1, '提现指定产品', 'CNY', 100.0000, 5.0000, 30, '1', '0', 1, 'admin', sysdate(), '', null, '认购后才可提现');
insert into biz_product values(2, 'USDT Product', 'USDT', 100.0000, 5.0000, 30, '1', '0', 2, 'admin', sysdate(), '', null, 'USDT withdraw required');

-- 业务参数（可重复执行）
delete from sys_config where config_id between 20 and 38;
insert into sys_config values(20, '签到奖励金额', 'biz.checkin.amount', '2', 'N', 'admin', sysdate(), '', null, '每日签到奖励人民币金额');
insert into sys_config values(21, '提现最低金额', 'biz.withdraw.minAmount', '105', 'N', 'admin', sysdate(), '', null, '人民币最低提现金额');
insert into sys_config values(22, '团队一级分佣比例', 'biz.team.rate.l1', '9', 'N', 'admin', sysdate(), '', null, '认购一级分佣百分比');
insert into sys_config values(23, '团队二级分佣比例', 'biz.team.rate.l2', '3', 'N', 'admin', sysdate(), '', null, '认购二级分佣百分比');
insert into sys_config values(24, '团队三级分佣比例', 'biz.team.rate.l3', '1', 'N', 'admin', sysdate(), '', null, '认购三级分佣百分比');
insert into sys_config values(25, '邀请奖励金额', 'biz.invite.reward', '0', 'N', 'admin', sysdate(), '', null, '邀请好友奖励，0表示暂无奖励');
insert into sys_config values(26, 'USDT业务开关', 'biz.usdt.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示USDT充提暂未开放');
insert into sys_config values(27, 'USDT min withdraw', 'biz.withdraw.minAmount.usdt', '105', 'N', 'admin', sysdate(), '', null, 'USDT min withdraw');
insert into sys_config values(28, '签到第一档连续天数', 'biz.checkin.prize1.days', '180', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(29, '签到第一档奖品', 'biz.checkin.prize1.name', '华为手机', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(30, '签到第一档中奖概率', 'biz.checkin.prize1.rate', '1', 'N', 'admin', sysdate(), '', null, '百分数，1表示1%，100表示必中');
insert into sys_config values(31, '签到第一档开关', 'biz.checkin.prize1.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');
insert into sys_config values(32, '签到第二档连续天数', 'biz.checkin.prize2.days', '365', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(33, '签到第二档奖品', 'biz.checkin.prize2.name', '华硕ROG笔记本电脑', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(34, '签到第二档中奖概率', 'biz.checkin.prize2.rate', '0.5', 'N', 'admin', sysdate(), '', null, '百分数，0.5表示0.5%，100表示必中');
insert into sys_config values(35, '签到第二档开关', 'biz.checkin.prize2.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');
insert into sys_config values(36, '谷歌验证开关', 'biz.google.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭谷歌验证');
insert into sys_config values(37, '提现必须谷歌验证', 'biz.google.requireWithdraw', 'false', 'N', 'admin', sysdate(), '', null, 'App提现不校验谷歌验证');
insert into sys_config values(38, '谷歌验证器名称', 'biz.google.issuer', 'App', 'N', 'admin', sysdate(), '', null, '显示在谷歌验证器中的名称');

delete from sys_config where config_id = 81 or config_key = 'biz.withdraw.feeRate';
insert into sys_config values(81, '提现手续费比例', 'biz.withdraw.feeRate', '3', 'N', 'admin', sysdate(), '', null, '百分数，3表示3%，0表示免手续费');

-- 每日返利任务（默认开启）
delete from sys_job where job_id = 100;
insert into sys_job values(100, '产品每日返利', 'DEFAULT', 'dailyRebateTask.execute()', '0 5 0 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '持仓订单每日返利');


-- App首页运行概览（展示用，后台手改）
drop table if exists biz_overview;
create table biz_overview (
  item_id           bigint(20)      not null auto_increment    comment '卡片ID',
  item_key          varchar(32)     not null                   comment '卡片标识，App用它匹配本地图',
  title             varchar(64)     not null                   comment '标题',
  display_value     varchar(64)     not null                   comment '展示数值，含单位',
  status_text       varchar(64)     default ''                 comment '状态文案',
  status_color      varchar(16)     default '#4DA3FF'          comment '状态点颜色',
  image_url         varchar(500)    default ''                 comment '可选配图，空则App用本地图',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (item_id),
  unique key uk_biz_overview_key (item_key)
) engine=innodb comment = 'App运行概览';

insert into biz_overview values(1, 'satellite', '在轨卫星', '320 颗', '正常运行', '#3DDC84', '', 1, '0', 'admin', sysdate(), '', null, null);
insert into biz_overview values(2, 'coverage', '覆盖国家/地区', '150+', '正常运行', '#4DA3FF', '', 2, '0', 'admin', sysdate(), '', null, null);
insert into biz_overview values(3, 'terminal', '在线终端', '1256000+', '稳定连接', '#4DA3FF', '', 3, '0', 'admin', sysdate(), '', null, null);


-- App关于我们（展示用，后台手改）
drop table if exists biz_about;
create table biz_about (
  about_id          bigint(20)      not null auto_increment    comment '内容ID',
  title             varchar(100)    not null                   comment '标题',
  subtitle          varchar(200)    default ''                 comment '副标题',
  content           mediumtext                                 comment '正文，后台富文本',
  image_url         varchar(500)    default ''                 comment '可选配图',
  display_mode      varchar(16)     not null default 'TEXT'    comment '展示模式 TEXT/PDF',
  pdf_url           varchar(500)    default ''                 comment 'PDF文件地址',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (about_id)
) engine=innodb comment = 'App关于我们（全局一条）';

insert into biz_about values(1, '星帆智联', '连接星空 · 智联未来', '<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>', '', 'TEXT', '', 1, '0', 'admin', sysdate(), '', null, null);


-- App官方群聊（展示用，后台上传二维码）
drop table if exists biz_group_chat;
create table biz_group_chat (
  group_id          bigint(20)      not null auto_increment    comment '群聊ID',
  title             varchar(100)    not null                   comment '标题',
  hint              varchar(100)    default '扫码进群'          comment '二维码下方提示',
  qr_url            varchar(500)    default ''                 comment '群聊二维码图片地址',
  remark            varchar(500)    default ''                 comment '补充说明',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (group_id)
) engine=innodb comment = 'App官方群聊';

insert into biz_group_chat values(1, '官方群聊', '扫码进群', '', '', 1, '0', 'admin', sysdate(), '', null);


-- App新闻资讯（展示用，后台手改）
drop table if exists biz_news;
create table biz_news (
  news_id           bigint(20)      not null auto_increment    comment '新闻ID',
  title             varchar(200)    not null                   comment '标题',
  summary           varchar(500)    default ''                 comment '摘要',
  cover_url         varchar(500)    default ''                 comment '封面图',
  content           mediumtext                                 comment '正文，后台富文本',
  publish_time      datetime                                   comment '发布日期',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (news_id)
) engine=innodb comment = 'App新闻资讯';

insert into biz_news values(1, '俄罗斯近24小时遥感卫星观测任务与行业应用动态', '俄罗斯近24小时遥感卫星观测任务与行业应用动态', '', '<p>一、在轨遥感星座整体运行工况平稳</p><p>（一）高分辨率光学卫星完成农情、地质重点区域成像。</p><p>（二）雷达卫星持续开展全天候云雨覆盖区域观测。</p><p>二、行业应用动态</p><p>面向应急、农业、交通等场景的数据产品按计划分发，支撑多地业务系统稳定运行。</p>', '2026-08-18 00:00:00', 1, '0', 'admin', sysdate(), '', null, null);
insert into biz_news values(2, '商业航天星座组网加速，行业应用场景持续拓展', '商业航天星座组网加速，行业应用场景持续拓展', '', '<p>商业航天正从单星验证走向规模组网。星帆智联持续推进星座部署与地面终端协同，为行业用户提供稳定连接能力。</p>', '2026-08-12 00:00:00', 2, '0', 'admin', sysdate(), '', null, null);


drop table if exists biz_carousel;
create table biz_carousel (
  carousel_id       bigint(20)      not null auto_increment    comment '轮播ID',
  title             varchar(100)    default ''                 comment '后台备注标题',
  video_url         varchar(500)    not null                   comment '视频地址',
  cover_url         varchar(500)    default ''                 comment '封面图，未播前展示',
  sort              int(4)          default 0                  comment '排序，越小越靠前',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (carousel_id)
) engine=innodb comment = 'App首页视频轮播';

-- ----------------------------
-- 业务菜单
-- ----------------------------
delete from sys_role_menu where menu_id >= 2000;
delete from sys_menu where menu_id >= 2000;
insert into sys_menu values('2000', '业务管理', '0', '5', 'biz', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, '认购返利业务目录');
insert into sys_menu values('2025', '产品交易', '0', '6', 'trade', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '产品、认购与签到目录');
insert into sys_menu values('2024', '运营内容', '0', '7', 'content', null, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, 'App展示与运营内容目录');
insert into sys_menu values('2001', '会员管理', '2000', '1', 'member', 'biz/member/index', '', '', 1, 0, 'C', '0', '0', 'biz:member:list', 'user', 'admin', sysdate(), '', null, 'C端会员');
insert into sys_menu values('2002', '产品管理', '2025', '2', 'product', 'biz/product/index', '', '', 1, 0, 'C', '0', '0', 'biz:product:list', 'shopping', 'admin', sysdate(), '', null, '认购产品');
insert into sys_menu values('2003', '认购订单', '2025', '3', 'order', 'biz/order/index', '', '', 1, 0, 'C', '0', '0', 'biz:order:list', 'list', 'admin', sysdate(), '', null, '认购订单');
insert into sys_menu values('2004', '签到记录', '2025', '4', 'checkin', 'biz/checkin/index', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:list', 'date', 'admin', sysdate(), '', null, '签到记录');
insert into sys_menu values('2005', '充值审核', '2000', '5', 'recharge', 'biz/recharge/index', '', '', 1, 0, 'C', '0', '0', 'biz:recharge:list', 'edit', 'admin', sysdate(), '', null, '充值审核');
insert into sys_menu values('2006', '提现审核', '2000', '6', 'withdraw', 'biz/withdraw/index', '', '', 1, 0, 'C', '0', '0', 'biz:withdraw:list', 'edit', 'admin', sysdate(), '', null, '提现审核');
insert into sys_menu values('2007', '资金流水', '2000', '7', 'walletLog', 'biz/walletLog/index', '', '', 1, 0, 'C', '0', '0', 'biz:walletLog:list', 'log', 'admin', sysdate(), '', null, '资金流水');
insert into sys_menu values('2008', '团队关系', '2000', '8', 'team', 'biz/team/index', '', '', 1, 0, 'C', '0', '0', 'biz:team:list', 'tree', 'admin', sysdate(), '', null, '团队关系');
insert into sys_menu values('2009', '会员等级', '2000', '9', 'level', 'biz/level/index', '', '', 1, 0, 'C', '0', '0', 'biz:level:list', 'peoples', 'admin', sysdate(), '', null, '会员等级');
insert into sys_menu values('2010', '分佣记录', '2000', '10', 'commission', 'biz/commission/index', '', '', 1, 0, 'C', '0', '0', 'biz:commission:list', 'form', 'admin', sysdate(), '', null, '分佣记录');
insert into sys_menu values('2011', '签到规则', '2000', '4', 'checkinRule', 'biz/checkin/rule', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:rule', 'edit', 'admin', sysdate(), '', null, '签到金额与连续抽奖规则');
insert into sys_menu values('2012', '签到中奖', '2000', '4', 'checkinPrize', 'biz/checkin/prize', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:prize', 'star', 'admin', sysdate(), '', null, '连续签到抽奖记录');
insert into sys_menu values('2013', '运行概览', '2024', '6', 'overview', 'biz/overview/index', '', '', 1, 0, 'C', '0', '0', 'biz:overview:list', 'dashboard', 'admin', sysdate(), '', null, 'App首页展示数字，后台手改');
insert into sys_menu values('2014', '关于我们', '2024', '5', 'about', 'biz/about/index', '', '', 1, 0, 'C', '0', '0', 'biz:about:list', 'guide', 'admin', sysdate(), '', null, 'App关于我们，后台手改');
insert into sys_menu values('2015', '官方群聊', '2024', '4', 'groupChat', 'biz/groupChat/index', '', '', 1, 0, 'C', '0', '0', 'biz:group:list', 'message', 'admin', sysdate(), '', null, 'App官方群聊二维码，后台手改');
insert into sys_menu values('2016', '新闻资讯', '2024', '3', 'news', 'biz/news/index', '', '', 1, 0, 'C', '0', '0', 'biz:news:list', 'documentation', 'admin', sysdate(), '', null, 'App新闻资讯，后台手改');
insert into sys_menu values('2018', '视频轮播', '2024', '2', 'carousel', 'biz/carousel/index', '', '', 1, 0, 'C', '0', '0', 'biz:carousel:list', 'example', 'admin', sysdate(), '', null, 'App首页视频轮播');

-- 按钮权限
insert into sys_menu values('2101', '会员查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2102', '会员修改', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2103', 'Member Add', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:member:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2111', '产品查询', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2112', '产品新增', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2113', '产品修改', '2002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2114', '产品删除', '2002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:product:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2121', '订单查询', '2003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:order:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2131', '签到查询', '2004', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2132', '签到规则保存', '2011', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:rule', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2133', '签到中奖查询', '2012', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:prize', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2141', '充值查询', '2005', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:recharge:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2142', '充值新增', '2005', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:recharge:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2143', '充值审核', '2005', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:recharge:audit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2151', '提现查询', '2006', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:withdraw:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2152', '提现审核', '2006', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:withdraw:audit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2161', '流水查询', '2007', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletLog:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2171', '团队查询', '2008', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2181', '等级查询', '2009', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2182', '等级新增', '2009', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2183', '等级修改', '2009', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2184', '等级删除', '2009', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:level:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2191', '分佣查询', '2010', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:commission:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2201', '概览查询', '2013', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2202', '概览新增', '2013', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2203', '概览修改', '2013', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2204', '概览删除', '2013', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2211', '关于查询', '2014', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2212', '关于新增', '2014', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2213', '关于修改', '2014', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2214', '关于删除', '2014', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2221', '群聊查询', '2015', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2222', '群聊新增', '2015', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2223', '群聊修改', '2015', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2224', '群聊删除', '2015', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2231', '新闻查询', '2016', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2232', '新闻新增', '2016', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2233', '新闻修改', '2016', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2234', '新闻删除', '2016', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2251', '轮播查询', '2018', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2252', '轮播新增', '2018', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2253', '轮播修改', '2018', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2254', '轮播删除', '2018', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- sys_google_auth_patch.sql ----------
-- 后台账号谷歌验证器（可重复执行）
DROP PROCEDURE IF EXISTS patch_sys_google_auth;
DELIMITER $$
CREATE PROCEDURE patch_sys_google_auth()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'ga_status'
  ) THEN
    ALTER TABLE sys_user
      ADD COLUMN ga_secret varchar(64) default '' comment '谷歌验证密钥' AFTER status,
      ADD COLUMN ga_status char(1) default '0' comment '谷歌验证（0未绑定 1已绑定）' AFTER ga_secret;
  END IF;
END$$
DELIMITER ;
CALL patch_sys_google_auth();
DROP PROCEDURE IF EXISTS patch_sys_google_auth;

delete from sys_config where config_id in (39, 40);
insert into sys_config values(39, '后台谷歌验证开关', 'sys.google.enabled', 'true', 'Y', 'admin', sysdate(), '', null, 'false表示后台登录不校验谷歌验证码');
insert into sys_config values(40, '后台谷歌验证器名称', 'sys.google.issuer', '后台管理', 'Y', 'admin', sysdate(), '', null, '显示在谷歌验证器中的名称');

-- ---------- biz_google_auth_patch.sql ----------
-- 谷歌验证器（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_google_auth;
DELIMITER $$
CREATE PROCEDURE patch_biz_google_auth()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_member' AND COLUMN_NAME = 'ga_status'
  ) THEN
    ALTER TABLE biz_member
      ADD COLUMN ga_secret varchar(64) default '' comment '谷歌验证密钥' AFTER status,
      ADD COLUMN ga_status char(1) default '0' comment '谷歌验证（0未绑定 1已绑定）' AFTER ga_secret;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_google_auth();
DROP PROCEDURE IF EXISTS patch_biz_google_auth;

delete from sys_config where config_id between 36 and 38;
insert into sys_config values(36, '谷歌验证开关', 'biz.google.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭谷歌验证');
insert into sys_config values(37, '提现必须谷歌验证', 'biz.google.requireWithdraw', 'false', 'N', 'admin', sysdate(), '', null, 'App提现不校验谷歌验证');
insert into sys_config values(38, '谷歌验证器名称', 'biz.google.issuer', 'App', 'N', 'admin', sysdate(), '', null, '显示在谷歌验证器中的名称');

-- ---------- biz_app_google_optional_patch.sql ----------
-- App 会员不需要谷歌验证：提现、登录均不校验。后台账号谷歌验证不受影响。
UPDATE sys_config
   SET config_value = 'false',
       remark = 'App提现和登录不校验谷歌验证'
 WHERE config_key = 'biz.google.requireWithdraw';

-- ---------- biz_invite_code_patch.sql ----------
-- 已有会员邀请码改为 7 位随机数字（1000000-9999999），不与现有邀请码重复
DROP PROCEDURE IF EXISTS patch_biz_invite_code;
DELIMITER $$
CREATE PROCEDURE patch_biz_invite_code()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE mid BIGINT;
  DECLARE new_code VARCHAR(7);
  DECLARE cur CURSOR FOR
    SELECT member_id FROM biz_member
    WHERE invite_code IS NULL OR CHAR_LENGTH(invite_code) <> 7 OR invite_code NOT REGEXP '^[0-9]{7}$';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO mid;
    IF done = 1 THEN
      LEAVE read_loop;
    END IF;
    retry: LOOP
      SET new_code = CAST(FLOOR(1000000 + RAND() * 9000000) AS CHAR);
      IF NOT EXISTS (SELECT 1 FROM biz_member WHERE invite_code = new_code) THEN
        UPDATE biz_member SET invite_code = new_code WHERE member_id = mid;
        LEAVE retry;
      END IF;
    END LOOP;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;
CALL patch_biz_invite_code();
DROP PROCEDURE IF EXISTS patch_biz_invite_code;

-- ---------- biz_pay_password_patch.sql ----------
-- App 支付/交易密码（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_pay_password;
DELIMITER $$
CREATE PROCEDURE patch_biz_pay_password()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_member' AND COLUMN_NAME = 'pay_password'
  ) THEN
    ALTER TABLE biz_member
      ADD COLUMN pay_password varchar(100) default '' comment '支付/交易密码' AFTER password;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_pay_password();
DROP PROCEDURE IF EXISTS patch_biz_pay_password;

-- ---------- biz_member_level_null_patch.sql ----------
SET NAMES utf8mb4;
-- 注册无默认等级：level_id 允许为空（可重复执行）
-- 不要把新会员写成 1：别的环境 id=1 可能是真实等级
alter table biz_member modify column level_id bigint(20) default null comment '会员等级ID，空表示无等级';

-- 只清掉指向已不存在等级的脏数据；id=1 若在 biz_level 里真实存在则不改
update biz_member m
left join biz_level l on l.level_id = m.level_id
set m.level_id = null
where m.level_id is not null and l.level_id is null;

-- ---------- biz_currency_patch.sql ----------
SET NAMES utf8mb4;
-- CNY / USDT independent settlement（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'currency');
set @sql := if(@exist = 0, 'alter table biz_product add column currency varchar(16) not null default ''CNY'' comment ''CNY/USDT'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'currency');
set @sql := if(@exist = 0, 'alter table biz_order add column currency varchar(16) not null default ''CNY'' comment ''CNY/USDT'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_rebate_log' and column_name = 'currency');
set @sql := if(@exist = 0, 'alter table biz_rebate_log add column currency varchar(16) not null default ''CNY'' comment ''CNY/USDT'' after member_id', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

UPDATE sys_config
   SET config_value = 'true'
 WHERE config_key = 'biz.usdt.enabled';

DELETE FROM sys_config WHERE config_id = 27 OR config_key = 'biz.withdraw.minAmount.usdt';
INSERT INTO sys_config VALUES (27, 'USDT min withdraw', 'biz.withdraw.minAmount.usdt', '105', 'N', 'admin', sysdate(), '', NULL, 'USDT min withdraw');

INSERT INTO biz_product (product_id, product_name, currency, price, daily_rebate, duration_days, withdraw_required, status, sort, create_by, create_time, remark)
SELECT 2, 'USDT Product', 'USDT', 100.0000, 5.0000, 30, '1', '0', 2, 'admin', sysdate(), 'USDT withdraw required'
WHERE NOT EXISTS (SELECT 1 FROM biz_product WHERE product_id = 2);

-- ---------- biz_checkin_rule_patch.sql ----------
-- 签到规则配置化 + 连续签到抽奖（可重复执行）
create table if not exists biz_checkin_prize (
  prize_log_id      bigint(20)      not null auto_increment    comment '抽奖记录ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  checkin_id        bigint(20)      not null                   comment '签到ID',
  streak_days       int(11)         not null                   comment '连续签到天数',
  prize_name        varchar(100)    not null                   comment '奖品名称',
  won               char(1)         not null default '0'       comment '是否中奖（0未中 1已中）',
  create_time       datetime                                   comment '创建时间',
  primary key (prize_log_id),
  unique key uk_biz_checkin_prize_once (member_id, checkin_id, streak_days),
  key idx_biz_checkin_prize_member (member_id, won)
) engine=innodb comment = '签到连续抽奖记录';

delete from sys_config where config_id between 28 and 35;
insert into sys_config values(28, '签到第一档连续天数', 'biz.checkin.prize1.days', '180', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(29, '签到第一档奖品', 'biz.checkin.prize1.name', '华为手机', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(30, '签到第一档中奖概率', 'biz.checkin.prize1.rate', '1', 'N', 'admin', sysdate(), '', null, '百分数，1表示1%，100表示必中');
insert into sys_config values(31, '签到第一档开关', 'biz.checkin.prize1.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');
insert into sys_config values(32, '签到第二档连续天数', 'biz.checkin.prize2.days', '365', 'N', 'admin', sysdate(), '', null, '连续签到满该天数触发抽奖');
insert into sys_config values(33, '签到第二档奖品', 'biz.checkin.prize2.name', '华硕ROG笔记本电脑', 'N', 'admin', sysdate(), '', null, '连续签到奖品名称');
insert into sys_config values(34, '签到第二档中奖概率', 'biz.checkin.prize2.rate', '0.5', 'N', 'admin', sysdate(), '', null, '百分数，0.5表示0.5%，100表示必中');
insert into sys_config values(35, '签到第二档开关', 'biz.checkin.prize2.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭该档抽奖');

delete from sys_menu where menu_id in (2011, 2012, 2132, 2133);
insert into sys_menu values('2011', '签到规则', '2000', '4', 'checkinRule', 'biz/checkin/rule', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:rule', 'edit', 'admin', sysdate(), '', null, '签到金额与连续抽奖规则');
insert into sys_menu values('2012', '签到中奖', '2000', '4', 'checkinPrize', 'biz/checkin/prize', '', '', 1, 0, 'C', '0', '0', 'biz:checkin:prize', 'star', 'admin', sysdate(), '', null, '连续签到抽奖记录');
insert into sys_menu values('2132', '签到规则保存', '2011', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:rule', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2133', '签到中奖查询', '2012', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:checkin:prize', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_product_schema_sync_patch.sql ----------
SET NAMES utf8mb4;
-- 补齐 biz_product 缺列，避免 /biz/product/list 报 Unknown column 'unlock_direct_qty'
-- 可重复执行；在业务库执行后刷新产品列表即可，然后重启 Java

-- 产品系列表，列表 left join 用：
create table if not exists biz_product_category (
  category_id       bigint(20)      not null auto_increment    comment '分类/系列ID',
  category_name     varchar(100)    not null                   comment '系列名称',
  cover_url         varchar(500)    default ''                 comment '封面图',
  status            char(1)         default '0'                comment '0显示 1隐藏',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (category_id)
) engine=innodb comment = '产品分类/系列';

-- biz_product 列表查询用到的列
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'name_en');
set @sql := if(@exist = 0, 'alter table biz_product add column name_en varchar(100) default '''' comment ''英文名'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'category_id');
set @sql := if(@exist = 0, 'alter table biz_product add column category_id bigint(20) default null comment ''所属分类/系列ID'' after name_en', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'price_cny');
set @sql := if(@exist = 0, 'alter table biz_product add column price_cny decimal(18,4) default null comment ''人民币认购价'' after price', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'price_usdt');
set @sql := if(@exist = 0, 'alter table biz_product add column price_usdt decimal(18,4) default null comment ''USDT认购价'' after price_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'daily_rebate_cny');
set @sql := if(@exist = 0, 'alter table biz_product add column daily_rebate_cny decimal(18,4) default null comment ''人民币每日返利'' after daily_rebate', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'daily_rebate_usdt');
set @sql := if(@exist = 0, 'alter table biz_product add column daily_rebate_usdt decimal(18,4) default null comment ''USDT每日返利'' after daily_rebate_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'buy_limit');
set @sql := if(@exist = 0, 'alter table biz_product add column buy_limit int(11) default 0 comment ''每人限购数量，0不限制'' after withdraw_required', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_direct_qty');
set @sql := if(@exist = 0, 'alter table biz_product add column unlock_direct_qty int(11) not null default 0 comment ''直推下级认购同一产品数量，0关闭'' after buy_limit', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_delay_hours');
set @sql := if(@exist = 0, 'alter table biz_product add column unlock_delay_hours int(11) not null default 0 comment ''认购完成后等待小时数再开始返利'' after unlock_direct_qty', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_rule_text');
set @sql := if(@exist = 0, 'alter table biz_product add column unlock_rule_text varchar(500) default '''' comment ''激活条件文案，App原样展示'' after unlock_delay_hours', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'payout_method');
set @sql := if(@exist = 0, 'alter table biz_product add column payout_method varchar(100) default '''' comment ''收益发放方式'' after unlock_delay_hours', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'risk_level');
set @sql := if(@exist = 0, 'alter table biz_product add column risk_level varchar(64) default '''' comment ''风险等级'' after payout_method', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'cover_url');
set @sql := if(@exist = 0, 'alter table biz_product add column cover_url varchar(500) default '''' comment ''产品封面图'' after sort', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set buy_limit = 0 where buy_limit is null;
update biz_product set price_cny = price, daily_rebate_cny = daily_rebate
 where price_cny is null and (currency is null or currency = '' or upper(currency) = 'CNY');
update biz_product set price_usdt = price, daily_rebate_usdt = daily_rebate
 where price_usdt is null and upper(ifnull(currency,'')) = 'USDT';

-- 订单一次购买份数，避免认购/返利用到列缺失
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'quantity');
set @sql := if(@exist = 0, 'alter table biz_order add column quantity int(11) not null default 1 comment ''认购份数'' after price', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_direct_qty');
set @sql := if(@exist = 0, 'alter table biz_order add column unlock_direct_qty int(11) not null default 0 comment ''下单时一次购买的份数'' after withdraw_required', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_delay_hours');
set @sql := if(@exist = 0, 'alter table biz_order add column unlock_delay_hours int(11) not null default 0 comment ''下单时等待小时数'' after unlock_direct_qty', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_order' and column_name = 'income_start_time');
set @sql := if(@exist = 0, 'alter table biz_order add column income_start_time datetime default null comment ''收益开始时间'' after unlock_delay_hours', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- biz_product_category_patch.sql ----------
-- App 产品系列（后台叫产品分类）。Tab 渲染系列，点进去查该系列下产品。
-- 可重复执行：建表/菜单/加列均可重复。

create table if not exists biz_product_category (
  category_id       bigint(20)      not null auto_increment    comment '分类/系列ID',
  category_name     varchar(100)    not null                   comment '系列名称',
  cover_url         varchar(500)    default ''                 comment '封面图，Tab 卡片用',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  sort              int(4)          default 0                  comment '排序，越小越靠前',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (category_id)
) engine=innodb comment = '产品分类/系列';

insert into biz_product_category (category_id, category_name, cover_url, status, sort, create_by, create_time, remark)
select 1, '「星帆·天启计划」', '', '0', 1, 'admin', sysdate(), 'App产品Tab系列'
from dual where not exists (select 1 from biz_product_category where category_id = 1);

insert into biz_product_category (category_id, category_name, cover_url, status, sort, create_by, create_time, remark)
select 2, '「星帆·远征计划」', '', '0', 2, 'admin', sysdate(), 'App产品Tab系列'
from dual where not exists (select 1 from biz_product_category where category_id = 2);

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'category_id');
set @sql := if(@exist = 0, 'alter table biz_product add column category_id bigint(20) default null comment ''所属分类/系列ID'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'name_en');
set @sql := if(@exist = 0, 'alter table biz_product add column name_en varchar(100) default '''' comment ''英文名'' after product_name', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'cover_url');
set @sql := if(@exist = 0, 'alter table biz_product add column cover_url varchar(500) default '''' comment ''产品封面图'' after sort', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set category_id = 1 where category_id is null;

delete from sys_menu where menu_id in (2017, 2241, 2242, 2243, 2244);
insert into sys_menu values('2017', '产品分类', '2025', '1', 'productCategory', 'biz/productCategory/index', '', '', 1, 0, 'C', '0', '0', 'biz:productCategory:list', 'cascader', 'admin', sysdate(), '', null, 'App产品Tab上的系列');
insert into sys_menu values('2241', '分类查询', '2017', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2242', '分类新增', '2017', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2243', '分类修改', '2017', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2244', '分类删除', '2017', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:productCategory:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_product_dual_price_patch.sql ----------
SET NAMES utf8mb4;
-- 同一产品同时配人民币/USDT 价格和日返。认购按所选币种扣对应钱包。（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'price_cny');
set @sql := if(@exist = 0, 'alter table biz_product add column price_cny decimal(18,4) default null comment ''人民币认购价'' after price', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'price_usdt');
set @sql := if(@exist = 0, 'alter table biz_product add column price_usdt decimal(18,4) default null comment ''USDT认购价'' after price_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'daily_rebate_cny');
set @sql := if(@exist = 0, 'alter table biz_product add column daily_rebate_cny decimal(18,4) default null comment ''人民币每日返利'' after daily_rebate', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'daily_rebate_usdt');
set @sql := if(@exist = 0, 'alter table biz_product add column daily_rebate_usdt decimal(18,4) default null comment ''USDT每日返利'' after daily_rebate_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product
   set price_cny = price, daily_rebate_cny = daily_rebate
 where price_cny is null
   and (currency is null or currency = '' or upper(currency) = 'CNY');

update biz_product
   set price_usdt = price, daily_rebate_usdt = daily_rebate
 where price_usdt is null
   and upper(currency) = 'USDT';

-- ---------- biz_product_buy_limit_patch.sql ----------
SET NAMES utf8mb4;
-- 产品限购：每人限购份数，0 表示不限制。（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_product' and column_name = 'buy_limit');
set @sql := if(@exist = 0, 'alter table biz_product add column buy_limit int(11) default 0 comment ''每人限购份数，0不限制'' after withdraw_required', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_product set buy_limit = 0 where buy_limit is null;

-- ---------- biz_product_display_patch.sql ----------
SET NAMES utf8mb4;
-- 产品展示字段：收益发放方式、风险等级（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'payout_method'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column payout_method varchar(100) default '''' comment ''收益发放方式，App展示'' after buy_limit',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'risk_level'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column risk_level varchar(64) default '''' comment ''风险等级，App展示'' after payout_method',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- ---------- biz_product_unlock_patch.sql ----------
SET NAMES utf8mb4;
-- 产品一拖二：直属下级认购同一产品达到份数后，再等 N 小时才开始日返（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_direct_qty'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column unlock_direct_qty int(11) not null default 0 comment ''直属下级需认购同一产品份数，0关闭'' after buy_limit',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_product' and column_name = 'unlock_delay_hours'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column unlock_delay_hours int(11) not null default 0 comment ''条件达成后等待小时数再开始收益'' after unlock_direct_qty',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_direct_qty'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column unlock_direct_qty int(11) not null default 0 comment ''下单时一拖二份数快照'' after withdraw_required',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_order' and column_name = 'unlock_delay_hours'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column unlock_delay_hours int(11) not null default 0 comment ''下单时等待小时快照'' after unlock_direct_qty',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_order' and column_name = 'income_start_time'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column income_start_time datetime default null comment ''收益开始时间'' after unlock_delay_hours',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- biz_product_on_sale_patch.sql ----------
SET NAMES utf8mb4;
-- 产品是否开售。可重复执行。现有产品默认开售。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'on_sale'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column on_sale char(1) not null default ''1'' comment ''1开售 0未开售'' after risk_level',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- ---------- biz_product_unlock_rule_text_patch.sql ----------
SET NAMES utf8mb4;
-- 产品激活条件文案。后台填写，App 原样展示。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_product'
    and column_name = 'unlock_rule_text'
);
set @sql := if(@exist = 0,
  'alter table biz_product add column unlock_rule_text varchar(500) default '''' comment ''激活条件文案，App原样展示'' after unlock_delay_hours',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- ---------- biz_order_quantity_patch.sql ----------
SET NAMES utf8mb4;
-- 认购订单支持数量（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_order'
    and column_name = 'quantity'
);
set @sql := if(@exist = 0,
  'alter table biz_order add column quantity int(11) not null default 1 comment ''认购份数'' after price',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- ---------- biz_order_unlock_lot_patch.sql ----------
SET NAMES utf8mb4;
-- 一拖二按份激活。可重复执行。

create table if not exists biz_order_unlock_lot (
  lot_id            bigint(20)      not null auto_increment    comment '批次ID',
  order_id          bigint(20)      not null                   comment '订单ID',
  share_no          int(11)         not null                   comment '该单内第几份，从0起',
  qty               int(11)         not null default 1         comment '份数',
  activate_time     datetime                                   comment '激活时间',
  income_start_time datetime                                   comment '开始返利时间',
  remaining_days    int(11)         not null                   comment '该份剩余返利天数',
  last_rebate_date  date            default null               comment '该份上次返利日期',
  create_time       datetime                                   comment '创建时间',
  primary key (lot_id),
  unique key uk_biz_order_unlock_lot (order_id, share_no),
  key idx_biz_order_unlock_lot_order (order_id)
) engine=innodb comment = '订单按份激活';

-- ---------- biz_subscribe_max_quantity_patch.sql ----------
SET NAMES utf8mb4;
-- 单次认购上限不再走系统参数，改由产品「每人限购」控制；0 或不填不限制（可重复执行）

delete from sys_config where config_key = 'biz.subscribe.maxQuantity';

-- ---------- biz_subscribe_commission_patch.sql ----------
SET NAMES utf8mb4;
-- 团队返佣改为认购触发（可重复执行，不删菜单）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_commission_log'
    and column_name = 'order_id'
);
set @sql := if(@exist = 0,
  'alter table biz_commission_log add column order_id bigint(20) default null comment ''认购订单ID'' after recharge_id',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update sys_config set remark = 'false关闭认购三级返佣' where config_key = 'biz.team.enabled';
update sys_config set remark = '认购一级分佣百分比' where config_key = 'biz.team.rate.l1';
update sys_config set remark = '认购二级分佣百分比' where config_key = 'biz.team.rate.l2';
update sys_config set remark = '认购三级分佣百分比' where config_key = 'biz.team.rate.l3';

-- ---------- biz_level_schema_sync_patch.sql ----------
SET NAMES utf8mb4;
-- 补齐 biz_level 缺列，避免 /biz/level/list 报 Unknown column 'performance_source'
-- 可重复执行；在业务库（如 ry-vue）跑一遍即可，然后重启 Java

-- team_depth
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'team_depth');
set @sql := if(@exist = 0, 'alter table biz_level add column team_depth varchar(50) default '''' comment ''team depth'' after min_valid_members', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- performance_source
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'performance_source');
set @sql := if(@exist = 0, 'alter table biz_level add column performance_source varchar(16) not null default ''SUBSCRIBE'' comment ''SUBSCRIBE/RECHARGE/BOTH'' after team_depth', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_recharge_cny
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_recharge_cny decimal(18,4) not null default 0 comment ''team recharge CNY'' after min_recharge_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_recharge_usdt
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_recharge_usdt decimal(18,4) not null default 0 comment ''team recharge USDT'' after min_team_recharge_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_perf_cny
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_cny decimal(18,4) default 0 comment ''team perf CNY'' after min_team_recharge_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- min_team_perf_usdt
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_usdt decimal(18,4) default 0 comment ''team perf USDT'' after min_team_perf_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- reward_*
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_enabled');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_enabled char(1) default ''0'' comment ''reward enabled'' after remark', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cycle');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cycle varchar(20) default ''NONE'' comment ''reward cycle'' after reward_enabled', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_mode');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_mode varchar(20) default ''AUTO'' comment ''reward mode'' after reward_cycle', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_repeat');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_repeat varchar(20) default ''NONE'' comment ''reward repeat'' after reward_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cny decimal(18,4) default 0 comment ''reward CNY'' after reward_repeat', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_usdt decimal(18,4) default 0 comment ''reward USDT'' after reward_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- wallet / valid member rules
set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'wallet_type_code');
set @sql := if(@exist = 0, 'alter table biz_level add column wallet_type_code varchar(32) not null default ''PROMO'' comment ''wallet type'' after reward_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'mixed_pay_currency');
set @sql := if(@exist = 0, 'alter table biz_level add column mixed_pay_currency varchar(16) not null default ''USDT'' comment ''USDT/CNY/BOTH'' after wallet_type_code', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_kyc');
set @sql := if(@exist = 0, 'alter table biz_level add column valid_need_kyc char(1) not null default ''1'' comment ''need kyc'' after mixed_pay_currency', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_order');
set @sql := if(@exist = 0, 'alter table biz_level add column valid_need_order char(1) not null default ''1'' comment ''need order'' after valid_need_kyc', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_level_reward_grant (
  grant_id          bigint(20)      not null auto_increment    comment 'grant id',
  member_id         bigint(20)      not null                   comment 'member id',
  level_id          bigint(20)      not null                   comment 'level id',
  level_name        varchar(50)     default ''                 comment 'level name',
  cycle_key         varchar(40)     not null                   comment 'cycle key',
  grant_cycle       varchar(20)     default ''                 comment 'ONCE MONTHLY PERMANENT',
  grant_mode        varchar(20)     default ''                 comment 'AUTO MANUAL',
  currency          varchar(10)     default 'CNY'              comment 'currency',
  amount            decimal(18,4)   default 0                  comment 'amount',
  status            char(1)         default '0'                comment '0 pending 1 paid 2 reject',
  pay_by            varchar(64)     default ''                 comment 'payer',
  pay_time          datetime                                   comment 'pay time',
  create_time       datetime                                   comment 'create time',
  remark            varchar(500)    default null               comment 'remark',
  primary key (grant_id),
  unique key uk_level_reward_cycle (member_id, level_id, cycle_key),
  key idx_level_reward_member (member_id),
  key idx_level_reward_status (status)
) engine=innodb comment = 'level reward grant';

update biz_level set team_depth = '一级内' where level_name = '启航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '二级内' where level_name = '探索' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '三级内' where level_name = '开拓' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '四级内' where level_name = '星耀' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '五级内' where level_name = '领航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '六级内' where level_name = '星域' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '七级内' where level_name = '星链' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '' where team_depth is null;

-- ---------- biz_level_reward_patch.sql ----------
SET NAMES utf8mb4;
-- 星链伙伴成长激励金：等级奖励配置与发放（可重复执行）

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_enabled');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_enabled char(1) default ''0'' comment ''是否启用该等级奖励（0否 1是）'' after remark', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cycle');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cycle varchar(20) default ''NONE'' comment ''ONCE一次 MONTHLY每月 PERMANENT永久 NONE无'' after reward_enabled', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_mode');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_mode varchar(20) default ''AUTO'' comment ''AUTO自动入账 MANUAL客服发放'' after reward_cycle', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_repeat');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_repeat varchar(20) default ''NONE'' comment ''永久档领取：NONE MONTHLY UNLIMITED'' after reward_mode', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_cny decimal(18,4) default 0 comment ''奖励金额CNY'' after reward_repeat', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'reward_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column reward_usdt decimal(18,4) default 0 comment ''奖励金额USDT'' after reward_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_cny');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_cny decimal(18,4) default 0 comment ''最低团队业绩CNY，0表示不限制'' after min_recharge_usdt', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (select count(*) from information_schema.columns where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_perf_usdt');
set @sql := if(@exist = 0, 'alter table biz_level add column min_team_perf_usdt decimal(18,4) default 0 comment ''最低团队业绩USDT，0表示不限制'' after min_team_perf_cny', 'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

create table if not exists biz_level_reward_grant (
  grant_id          bigint(20)      not null auto_increment    comment '发放ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  level_id          bigint(20)      not null                   comment '等级ID',
  level_name        varchar(50)     default ''                 comment '等级名称快照',
  cycle_key         varchar(40)     not null                   comment '去重键 ONCE/yyyy-MM/ELIGIBLE/PAY-xxx',
  grant_cycle       varchar(20)     default ''                 comment 'ONCE MONTHLY PERMANENT',
  grant_mode        varchar(20)     default ''                 comment 'AUTO MANUAL',
  currency          varchar(10)     default 'CNY'              comment '发放币种',
  amount            decimal(18,4)   default 0                  comment '发放金额',
  status            char(1)         default '0'                comment '0待发放 1已发放 2已拒绝',
  pay_by            varchar(64)     default ''                 comment '发放人',
  pay_time          datetime                                   comment '发放时间',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (grant_id),
  unique key uk_level_reward_cycle (member_id, level_id, cycle_key),
  key idx_level_reward_member (member_id),
  key idx_level_reward_status (status)
) engine=innodb comment = '等级奖励发放记录';

delete from sys_config where config_id between 41 and 47;
insert into sys_config values(41, '等级奖励开关', 'biz.levelReward.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false表示关闭成长激励金');
insert into sys_config values(42, '混合业绩发放币种', 'biz.levelReward.mixedPayCurrency', 'USDT', 'N', 'admin', sysdate(), '', null, '团队同时有人民币和USDT业绩时发这个币种');
insert into sys_config values(43, '团队业绩口径', 'biz.levelReward.performanceSource', 'SUBSCRIBE', 'N', 'admin', sysdate(), '', null, 'SUBSCRIBE认购 RECHARGE充值 BOTH两者相加');
insert into sys_config values(44, '团队业绩含本人', 'biz.levelReward.includeSelf', 'false', 'N', 'admin', sysdate(), '', null, 'true表示本人业绩计入团队');
insert into sys_config values(45, '有效成员需实名', 'biz.levelReward.validNeedKyc', 'true', 'N', 'admin', sysdate(), '', null, '有效成员是否必须已实名');
insert into sys_config values(46, '有效成员需认购', 'biz.levelReward.validNeedOrder', 'true', 'N', 'admin', sysdate(), '', null, '有效成员是否必须有认购订单');
insert into sys_config values(47, '等级奖励规则说明', 'biz.levelReward.ruleText', '启航、探索、开拓、星耀、领航、星域：达成条件后系统自动发放1次成长激励金。星链：达成条件后联系客服领取，由后台手动发放。团队同时有人民币和USDT业绩时发放USDT。最终以系统核算为准。', 'N', 'admin', sysdate(), '', null, '展示给App/后台的规则文案');

-- 预置七档名称（停用，避免未配金额就升级）。后台改条件、金额后打开即可。
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '启航', 1, 0, 0, 0, 0, 10, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '启航');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '探索', 3, 0, 0, 0, 0, 20, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '探索');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '开拓', 5, 0, 0, 0, 0, 30, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '开拓');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '星耀', 10, 0, 0, 0, 0, 40, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '星耀');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '领航', 20, 0, 0, 0, 0, 50, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '领航');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '星域', 50, 0, 0, 0, 0, 60, '1', 'admin', sysdate(), '成长激励金：一次自动发放', '1', 'ONCE', 'AUTO', 'NONE', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '星域');
insert into biz_level (level_name, min_valid_members, min_recharge_cny, min_recharge_usdt, min_team_perf_cny, min_team_perf_usdt, sort, status, create_by, create_time, remark, reward_enabled, reward_cycle, reward_mode, reward_repeat, reward_cny, reward_usdt)
select '星链', 100, 0, 0, 0, 0, 70, '1', 'admin', sysdate(), '成长激励金：达标后联系客服，后台手动发放', '1', 'PERMANENT', 'MANUAL', 'UNLIMITED', 0, 0
from dual where not exists (select 1 from biz_level where level_name = '星链');

delete from sys_menu where menu_id in (2019, 2020, 2261, 2262, 2271, 2272, 2273);
insert into sys_menu values('2019', '等级奖励', '2000', '9', 'levelReward', 'biz/levelReward/index', '', '', 1, 0, 'C', '0', '0', 'biz:levelReward:list', 'money', 'admin', sysdate(), '', null, '成长激励金规则与各等级奖励');
insert into sys_menu values('2020', '等级奖励发放', '2000', '9', 'levelRewardGrant', 'biz/levelReward/grant', '', '', 1, 0, 'C', '0', '0', 'biz:levelReward:grant', 'list', 'admin', sysdate(), '', null, '客服确认发放成长激励金');
insert into sys_menu values('2261', '奖励查询', '2019', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2262', '奖励修改', '2019', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2271', '发放查询', '2020', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:grant', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2272', '确认发放', '2020', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:pay', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2273', '拒绝发放', '2020', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:levelReward:reject', '#', 'admin', sysdate(), '', null, '');

delete from sys_job where job_id = 101;
insert into sys_job values(101, '等级奖励核算', 'DEFAULT', 'levelRewardTask.execute()', '0 15 0 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '每日核算成长激励金：前六档一次自动，星链生成待发放');

-- ---------- biz_level_reward_once_patch.sql ----------
-- 成长激励金：前六档一次自动发放，仅星链找客服、后台手动发放（可重复执行）

update biz_level
   set reward_cycle = 'ONCE',
       reward_mode = 'AUTO',
       reward_repeat = 'NONE',
       remark = '成长激励金：一次自动发放'
 where level_name in ('启航', '探索', '开拓', '星耀', '领航', '星域');

update biz_level
   set reward_cycle = 'PERMANENT',
       reward_mode = 'MANUAL',
       reward_repeat = 'UNLIMITED',
       remark = '成长激励金：达标后联系客服，后台手动发放'
 where level_name = '星链';

update sys_config
   set config_value = '启航、探索、开拓、星耀、领航、星域：达成条件后系统自动发放1次成长激励金。星链：达成条件后联系客服领取，由后台手动发放。团队同时有人民币和USDT业绩时发放USDT。最终以系统核算为准。'
 where config_key = 'biz.levelReward.ruleText';

update sys_job
   set remark = '每日核算成长激励金：前六档一次自动，星链生成待发放'
 where job_name = '等级奖励核算';

-- ---------- biz_level_copy_patch.sql ----------
-- App 会员等级页：规则说明 + 表格上方注释（可重复执行）

delete from sys_config where config_id = 51;
insert into sys_config values(51, '等级页注释', 'biz.levelReward.hint', '注：成员个人累计认购金额达到 ¥10,000 或 1,429 USDT 后，方可计入团队等级考核。请遵循平台规则，严禁作弊行为，一经发现将取消奖励资格。', 'N', 'admin', sysdate(), '', null, 'App会员等级页表格上方的注');

-- ---------- biz_level_perf_source_patch.sql ----------
SET NAMES utf8mb4;
-- 团队业绩口径下放到每个等级；可重复执行
-- 本人累计仍按充值；团队累计按该等级口径（认购/充值/认购+充值）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'performance_source'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column performance_source varchar(16) not null default ''SUBSCRIBE'' comment ''团队业绩口径 SUBSCRIBE认购 RECHARGE充值 BOTH认购+充值'' after team_depth',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @src := (
  select config_value from sys_config
  where config_key = 'biz.levelReward.performanceSource'
  limit 1
);
-- 还是建列默认值的等级，沿用原来的全局口径；已经改过的不覆盖
update biz_level
set performance_source = ifnull(@src, 'SUBSCRIBE')
where performance_source = 'SUBSCRIBE';

-- 把对话框里看不到的「团队业绩」数字挪到「团队累计」，避免两套门槛
update biz_level
set min_team_recharge_cny = min_team_perf_cny,
    min_team_recharge_usdt = min_team_perf_usdt
where ifnull(min_team_recharge_cny, 0) = 0
  and ifnull(min_team_recharge_usdt, 0) = 0
  and (ifnull(min_team_perf_cny, 0) > 0 or ifnull(min_team_perf_usdt, 0) > 0);

update biz_level
set min_team_perf_cny = 0, min_team_perf_usdt = 0
where ifnull(min_team_perf_cny, 0) > 0 or ifnull(min_team_perf_usdt, 0) > 0;

-- ---------- biz_wallet_type_patch.sql ----------
SET NAMES utf8mb4;
-- 钱包类型：余额 / 产品收益 / 推广收益 / 助力值。CNY、USDT 是类型下的币种。
-- 可重复执行。历史余额拆分只在尚未存在 PRODUCT 钱包时做一次。

create table if not exists biz_wallet_type (
  type_id           bigint(20)      not null auto_increment    comment '类型ID',
  type_code         varchar(32)     not null                   comment '类型编码',
  type_name         varchar(64)     not null                   comment '类型名称',
  withdraw_mode     varchar(32)     not null default 'NONE'    comment '提现规则 NONE不可提 OPEN可提 ANY_ORDER任意认购后 PRODUCT_REQUIRED指定产品',
  status            char(1)         default '0'                comment '状态 0正常 1停用',
  sort              int(4)          default 0                  comment '排序，越小越靠前',
  builtin           char(1)         default '0'                comment '内置 1不可删改编码',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (type_id),
  unique key uk_biz_wallet_type_code (type_code)
) engine=innodb comment = '钱包类型';

create table if not exists biz_wallet_credit_rule (
  rule_id           bigint(20)      not null auto_increment    comment '规则ID',
  biz_type          varchar(32)     not null                   comment '业务类型',
  biz_name          varchar(64)     not null                   comment '业务名称',
  type_code         varchar(32)     not null                   comment '入账钱包类型',
  builtin           char(1)         default '0'                comment '内置 1不可删除',
  sort              int(4)          default 0                  comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (rule_id),
  unique key uk_biz_wallet_credit_biz (biz_type)
) engine=innodb comment = '奖励入账钱包配置';

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'BALANCE', '余额', 'NONE', '0', 1, '1', 'admin', sysdate(), '充值进去的钱，不能提现，用于认购'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'BALANCE');

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'PRODUCT', '产品收益', 'PRODUCT_REQUIRED', '0', 2, '1', 'admin', sysdate(), '产品产生的收益，可以提现'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'PRODUCT');

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'PROMO', '推广收益', 'ANY_ORDER', '0', 3, '1', 'admin', sysdate(), '推广相关收益，认购任意产品后才能提现'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'PROMO');

insert into biz_wallet_type (type_code, type_name, withdraw_mode, status, sort, builtin, create_by, create_time, remark)
select 'ASSIST', '助力值', 'NONE', '0', 4, '1', 'admin', sysdate(), '暂定，不可提现'
from dual where not exists (select 1 from biz_wallet_type where type_code = 'ASSIST');

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'RECHARGE', '充值', 'BALANCE', '1', 1, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'RECHARGE');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'REBATE', '产品日返', 'PRODUCT', '1', 2, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'REBATE');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'CHECKIN', '签到奖励', 'PROMO', '1', 3, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'CHECKIN');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'KYC_REWARD', '实名认证奖励', 'PROMO', '1', 4, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'KYC_REWARD');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'INVITE', '邀请奖励', 'PROMO', '1', 5, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'INVITE');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'COMMISSION', '推广分佣', 'PROMO', '1', 6, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'COMMISSION');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'LEVEL_REWARD', '等级奖励', 'PROMO', '1', 7, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'LEVEL_REWARD');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'ADJUST', '后台调账默认', 'BALANCE', '1', 8, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'ADJUST');

drop procedure if exists biz_patch_wallet_type;

delimiter $$

create procedure biz_patch_wallet_type()
begin
  if not exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_wallet' and column_name = 'type_code') then
    alter table biz_wallet add column type_code varchar(32) not null default 'BALANCE' comment '钱包类型编码' after member_id;
  end if;
  if exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_wallet' and index_name = 'uk_biz_wallet_member_currency') then
    alter table biz_wallet drop index uk_biz_wallet_member_currency;
  end if;
  if not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_wallet' and index_name = 'uk_biz_wallet_member_type_currency') then
    alter table biz_wallet add unique key uk_biz_wallet_member_type_currency (member_id, type_code, currency);
  end if;
  if not exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_wallet_log' and column_name = 'type_code') then
    alter table biz_wallet_log add column type_code varchar(32) not null default 'BALANCE' comment '钱包类型编码' after member_id;
  end if;
  if not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_wallet_log' and index_name = 'idx_biz_wallet_log_type') then
    alter table biz_wallet_log add key idx_biz_wallet_log_type (member_id, type_code, create_time);
  end if;
  if not exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_withdraw' and column_name = 'wallet_type_code') then
    alter table biz_withdraw add column wallet_type_code varchar(32) default 'PRODUCT' comment '提现钱包类型' after currency;
  end if;

  update biz_withdraw
     set wallet_type_code = 'PROMO'
   where ifnull(wallet_type_code, '') in ('', 'PRODUCT')
     and (
       ifnull(remark, '') like '推广收益%'
       or upper(ifnull(remark, '')) like 'PROMO%'
       or upper(ifnull(remark, '')) like 'ASSIST%'
     );

  update biz_withdraw
     set wallet_type_code = 'PRODUCT'
   where ifnull(wallet_type_code, '') = '';

  update biz_wallet_log l
     set l.type_code = case
       when l.biz_type = 'REBATE' then 'PRODUCT'
       when l.biz_type in ('CHECKIN', 'KYC_REWARD', 'INVITE', 'COMMISSION', 'LEVEL_REWARD') then 'PROMO'
       when l.biz_type in ('WITHDRAW_FREEZE', 'WITHDRAW_SUCCESS', 'WITHDRAW_REJECT') then ifnull((
         select w.wallet_type_code from biz_withdraw w where w.withdraw_id = l.biz_id
       ), 'PRODUCT')
       else 'BALANCE'
     end
   where ifnull(l.type_code, 'BALANCE') = 'BALANCE'
     and l.biz_type in ('REBATE', 'CHECKIN', 'KYC_REWARD', 'INVITE', 'COMMISSION', 'LEVEL_REWARD',
                        'WITHDRAW_FREEZE', 'WITHDRAW_SUCCESS', 'WITHDRAW_REJECT');

  if (select count(*) from biz_wallet where type_code = 'PRODUCT') = 0 then
    drop temporary table if exists tmp_wallet_split;
    create temporary table tmp_wallet_split (
      member_id bigint(20) not null,
      currency varchar(16) not null,
      old_available decimal(18,4) not null default 0,
      old_frozen decimal(18,4) not null default 0,
      product_ideal decimal(18,4) not null default 0,
      promo_ideal decimal(18,4) not null default 0,
      product_frozen decimal(18,4) not null default 0,
      promo_frozen decimal(18,4) not null default 0,
      product_avail decimal(18,4) not null default 0,
      promo_avail decimal(18,4) not null default 0,
      balance_avail decimal(18,4) not null default 0,
      balance_frozen decimal(18,4) not null default 0,
      primary key (member_id, currency)
    );

    insert into tmp_wallet_split (member_id, currency, old_available, old_frozen, product_ideal, promo_ideal, product_frozen, promo_frozen)
    select w.member_id, w.currency, w.available, w.frozen,
           greatest(ifnull((
             select sum(case
               when l.biz_type = 'REBATE' then l.amount
               when l.biz_type in ('WITHDRAW_FREEZE', 'WITHDRAW_REJECT')
                    and l.available_before <> l.available_after
                    and ifnull((select wd.wallet_type_code from biz_withdraw wd where wd.withdraw_id = l.biz_id), 'PRODUCT') = 'PRODUCT'
               then l.amount
               else 0 end)
             from biz_wallet_log l
             where l.member_id = w.member_id and l.currency = w.currency
           ), 0), 0),
           greatest(ifnull((
             select sum(case
               when l.biz_type in ('CHECKIN', 'KYC_REWARD', 'INVITE', 'COMMISSION', 'LEVEL_REWARD') then l.amount
               when l.biz_type in ('WITHDRAW_FREEZE', 'WITHDRAW_REJECT')
                    and l.available_before <> l.available_after
                    and ifnull((select wd.wallet_type_code from biz_withdraw wd where wd.withdraw_id = l.biz_id), 'PRODUCT') = 'PROMO'
               then l.amount
               else 0 end)
             from biz_wallet_log l
             where l.member_id = w.member_id and l.currency = w.currency
           ), 0), 0),
           greatest(ifnull((
             select sum(wd.amount) from biz_withdraw wd
             where wd.member_id = w.member_id and wd.currency = w.currency
               and wd.status = '0' and ifnull(wd.wallet_type_code, 'PRODUCT') = 'PRODUCT'
           ), 0), 0),
           greatest(ifnull((
             select sum(wd.amount) from biz_withdraw wd
             where wd.member_id = w.member_id and wd.currency = w.currency
               and wd.status = '0' and ifnull(wd.wallet_type_code, 'PRODUCT') = 'PROMO'
           ), 0), 0)
    from biz_wallet w
    where ifnull(w.type_code, 'BALANCE') = 'BALANCE';

    update tmp_wallet_split
       set product_frozen = least(product_frozen, old_frozen),
           promo_frozen = least(promo_frozen, greatest(old_frozen - least(product_frozen, old_frozen), 0)),
           balance_frozen = greatest(old_frozen - least(product_frozen, old_frozen) - least(promo_frozen, greatest(old_frozen - least(product_frozen, old_frozen), 0)), 0),
           product_avail = least(product_ideal, old_available),
           promo_avail = least(promo_ideal, greatest(old_available - least(product_ideal, old_available), 0)),
           balance_avail = greatest(old_available - least(product_ideal, old_available) - least(promo_ideal, greatest(old_available - least(product_ideal, old_available), 0)), 0);

    update biz_wallet w
      join tmp_wallet_split t on t.member_id = w.member_id and t.currency = w.currency
       set w.available = t.balance_avail, w.frozen = t.balance_frozen, w.update_time = sysdate()
     where ifnull(w.type_code, 'BALANCE') = 'BALANCE';

    insert into biz_wallet (member_id, type_code, currency, available, frozen, create_time, update_time)
    select t.member_id, 'PRODUCT', t.currency, t.product_avail, t.product_frozen, sysdate(), sysdate()
    from tmp_wallet_split t
    where not exists (
      select 1 from biz_wallet x where x.member_id = t.member_id and x.type_code = 'PRODUCT' and x.currency = t.currency
    );

    insert into biz_wallet (member_id, type_code, currency, available, frozen, create_time, update_time)
    select t.member_id, 'PROMO', t.currency, t.promo_avail, t.promo_frozen, sysdate(), sysdate()
    from tmp_wallet_split t
    where not exists (
      select 1 from biz_wallet x where x.member_id = t.member_id and x.type_code = 'PROMO' and x.currency = t.currency
    );
  end if;

  insert into biz_wallet (member_id, type_code, currency, available, frozen, create_time, update_time)
  select m.member_id, t.type_code, c.currency, 0, 0, sysdate(), sysdate()
  from biz_member m
  cross join (select 'BALANCE' as type_code union all select 'PRODUCT' union all select 'PROMO' union all select 'ASSIST') t
  cross join (select 'CNY' as currency union all select 'USDT') c
  where not exists (
    select 1 from biz_wallet w
    where w.member_id = m.member_id and w.type_code = t.type_code and w.currency = c.currency
  );
end

$$
delimiter ;
call biz_patch_wallet_type();
drop procedure if exists biz_patch_wallet_type;

delete from sys_role_menu where menu_id in (2036, 2037, 2340, 2341, 2342, 2343, 2344, 2345, 2346, 2347);
delete from sys_menu where menu_id in (2036, 2037, 2340, 2341, 2342, 2343, 2344, 2345, 2346, 2347);

insert into sys_menu values('2036', '钱包类型', '2032', '13', 'walletType', 'biz/walletType/index', '', '', 1, 0, 'C', '0', '0', 'biz:walletType:list', 'nested', 'admin', sysdate(), '', null, '余额、产品收益、推广收益、助力值，可新增');
insert into sys_menu values('2340', '钱包类型查询', '2036', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2341', '钱包类型新增', '2036', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2342', '钱包类型修改', '2036', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2343', '钱包类型删除', '2036', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletType:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('2037', '奖励入账', '2032', '14', 'walletCredit', 'biz/walletCredit/index', '', '', 1, 0, 'C', '0', '0', 'biz:walletCredit:list', 'edit', 'admin', sysdate(), '', null, '签到、实名、邀请等奖励入到哪个钱包');
insert into sys_menu values('2344', '入账配置查询', '2037', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2345', '入账配置新增', '2037', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2346', '入账配置修改', '2037', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2347', '入账配置删除', '2037', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:walletCredit:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2036 as menu_id union all select 2037 union all select 2340 union all select 2341
  union all select 2342 union all select 2343 union all select 2344 union all select 2345
  union all select 2346 union all select 2347
) m
where rm.menu_id = 2007
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

insert ignore into sys_role_menu values
 (1, 2036), (1, 2037), (1, 2340), (1, 2341), (1, 2342), (1, 2343), (1, 2344), (1, 2345), (1, 2346), (1, 2347);

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PRODUCT', '产品收益提现', 'PRODUCT', '1', 9, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PRODUCT');
insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PROMO', '推广收益提现', 'PROMO', '1', 10, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PROMO');

-- ---------- biz_level_wallet_patch.sql ----------
SET NAMES utf8mb4;
-- 到账钱包、发放币种、有效成员规则下放到每个等级（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'wallet_type_code'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column wallet_type_code varchar(32) not null default ''PROMO'' comment ''到账钱包类型'' after reward_usdt',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'mixed_pay_currency'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column mixed_pay_currency varchar(16) not null default ''USDT'' comment ''发放币种 USDT/CNY/BOTH'' after wallet_type_code',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_kyc'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column valid_need_kyc char(1) not null default ''1'' comment ''有效成员需实名 1是 0否'' after mixed_pay_currency',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'valid_need_order'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column valid_need_order char(1) not null default ''1'' comment ''有效成员需认购 1是 0否'' after valid_need_kyc',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @wallet := 'PROMO';
set @has_credit := (
  select count(*) from information_schema.tables
  where table_schema = database() and table_name = 'biz_wallet_credit_rule'
);
set @sql := if(@has_credit > 0,
  'select ifnull((select type_code from biz_wallet_credit_rule where biz_type = ''LEVEL_REWARD'' limit 1), ''PROMO'') into @wallet',
  'select ''PROMO'' into @wallet');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_level
set wallet_type_code = ifnull(@wallet, 'PROMO')
where wallet_type_code = 'PROMO';

set @mixed := (
  select config_value from sys_config where config_key = 'biz.levelReward.mixedPayCurrency' limit 1
);
update biz_level
set mixed_pay_currency = ifnull(@mixed, 'USDT')
where mixed_pay_currency = 'USDT';

set @kyc := (
  select config_value from sys_config where config_key = 'biz.levelReward.validNeedKyc' limit 1
);
update biz_level
set valid_need_kyc = if(@kyc in ('false', '0', 'n', 'N'), '0', '1')
where valid_need_kyc = '1';

set @ord := (
  select config_value from sys_config where config_key = 'biz.levelReward.validNeedOrder' limit 1
);
update biz_level
set valid_need_order = if(@ord in ('false', '0', 'n', 'N'), '0', '1')
where valid_need_order = '1';

-- ---------- biz_level_team_recharge_patch.sql ----------
SET NAMES utf8mb4;
-- 等级门槛：下级累计充值（按团队要求层数统计，不含本人）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_cny'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column min_team_recharge_cny decimal(18,4) not null default 0 comment ''下级累计充值CNY，0不限制'' after min_recharge_usdt',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'min_team_recharge_usdt'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column min_team_recharge_usdt decimal(18,4) not null default 0 comment ''下级累计充值USDT，0不限制'' after min_team_recharge_cny',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- biz_level_team_depth_patch.sql ----------
SET NAMES utf8mb4;
-- App 会员等级表「团队要求」展示文案，接口字段 teamDepth（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'biz_level' and column_name = 'team_depth'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column team_depth varchar(50) default '''' comment ''团队要求，App等级表展示'' after min_valid_members',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

update biz_level set team_depth = '一级内' where level_name = '启航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '二级内' where level_name = '探索' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '三级内' where level_name = '开拓' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '四级内' where level_name = '星耀' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '五级内' where level_name = '领航' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '六级内' where level_name = '星域' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '七级内' where level_name = '星链' and (team_depth is null or team_depth = '');
update biz_level set team_depth = '' where team_depth is null;

-- ---------- biz_level_threshold_mode_patch.sql ----------
SET NAMES utf8mb4;
-- 等级门槛：分币种 SPLIT（默认，与现网一致） / 折合人民币 EQUIV
-- 汇率写在 sys_config.biz.fx.usdtToCny，默认 6.25（1 USDT = 6.25 CNY）
-- 可重复执行；跑完需重启 Java

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_level'
    and column_name = 'threshold_mode'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column threshold_mode varchar(16) not null default ''SPLIT'' comment ''SPLIT分币种同时达标 EQUIV折合人民币合计达标'' after performance_source',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'USDT to CNY rate', 'biz.fx.usdtToCny', '6.25', 'N', 'admin', sysdate(), '1 USDT = this many CNY; used by EQUIV level threshold'
from dual
where not exists (select 1 from sys_config where config_key = 'biz.fx.usdtToCny');

-- ---------- biz_level_team_threshold_mode_patch.sql ----------
SET NAMES utf8mb4;
-- Personal vs team threshold mode. Safe to re-run.
-- threshold_mode = personal; team_threshold_mode = team
-- First run copies current threshold_mode onto team so live levels stay the same.

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_level'
    and column_name = 'team_threshold_mode'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column team_threshold_mode varchar(16) not null default ''SPLIT'' comment ''team SPLIT/EQUIV'' after threshold_mode',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @sql := if(@exist = 0,
  'update biz_level set team_threshold_mode = ifnull(nullif(threshold_mode, ''''), ''SPLIT'')',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- ---------- biz_level_reward_claim_patch.sql ----------
SET NAMES utf8mb4;
-- 等级奖励：用户领取 + 二选一/都可领。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_level'
    and column_name = 'reward_claim_policy'
);
set @sql := if(@exist = 0,
  'alter table biz_level add column reward_claim_policy varchar(16) not null default ''ONE'' comment ''CLAIM时 ONE二选一 ALL都可领取'' after reward_mode',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- ---------- biz_team_depth_dict_patch.sql ----------
SET NAMES utf8mb4;
-- 团队要求改为字典 biz_team_depth：标签展示，键值是层数。可重复执行。
-- 新增例如：标签「八级内」，键值 8（表示第1到第8层都算）

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '团队要求层数', 'biz_team_depth', '0', 'admin', sysdate(), '一级内=直属，二级内=第1+2层。键值填层数'
from dual
where not exists (select 1 from sys_dict_type where dict_type = 'biz_team_depth');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '一级内', '1', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '只算直属下级'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '1');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '二级内', '2', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第2层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '2');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '三级内', '3', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第3层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '3');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '四级内', '4', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第4层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '4');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 5, '五级内', '5', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第5层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '5');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 6, '六级内', '6', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第6层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '6');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 7, '七级内', '7', 'biz_team_depth', '', 'default', 'N', '0', 'admin', sysdate(), '第1到第7层'
from dual where not exists (select 1 from sys_dict_data where dict_type = 'biz_team_depth' and dict_value = '7');

update biz_level l
inner join sys_dict_data d on d.dict_type = 'biz_team_depth' and d.dict_label = l.team_depth
set l.team_depth = d.dict_value
where l.team_depth is not null and l.team_depth <> '';

-- ---------- biz_fx_rate_log_patch.sql ----------
SET NAMES utf8mb4;
-- USDT to CNY rate change log (safe to re-run)

create table if not exists biz_fx_rate_log (
  log_id bigint not null auto_increment,
  old_rate decimal(18,6) not null default 0,
  new_rate decimal(18,6) not null default 0,
  operator varchar(64) default '',
  create_time datetime default null,
  remark varchar(500) default null,
  primary key (log_id)
) engine=innodb default charset=utf8mb4 comment='USDT to CNY rate change log';

-- ---------- biz_level_reward_menu_drop.sql ----------
SET NAMES utf8mb4;
-- 去掉「等级奖励」配置页菜单；查询/修改按钮挂到「会员等级」，「等级奖励发放」保留

delete from sys_role_menu where menu_id = 2019;
delete from sys_menu where menu_id = 2019;

update sys_menu set parent_id = 2009, order_num = 5 where menu_id = 2261;
update sys_menu set parent_id = 2009, order_num = 6 where menu_id = 2262;

-- ---------- biz_withdraw_pay_patch.sql ----------
-- 提现审核打款：收款方式 + 打款凭证（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_withdraw_pay;
DELIMITER $$
CREATE PROCEDURE patch_biz_withdraw_pay()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_withdraw' AND COLUMN_NAME = 'pay_method'
  ) THEN
    ALTER TABLE biz_withdraw
      ADD COLUMN pay_method varchar(32) default '' comment '收款方式（ALIPAY/USDT）' AFTER account_info;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_withdraw' AND COLUMN_NAME = 'pay_proof_url'
  ) THEN
    ALTER TABLE biz_withdraw
      ADD COLUMN pay_proof_url varchar(500) default '' comment '打款凭证图片' AFTER audit_remark;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_withdraw_pay();
DROP PROCEDURE IF EXISTS patch_biz_withdraw_pay;

UPDATE biz_withdraw SET pay_method = 'USDT' WHERE (pay_method is null or pay_method = '') AND currency = 'USDT';
UPDATE biz_withdraw SET pay_method = 'ALIPAY' WHERE (pay_method is null or pay_method = '') AND currency = 'CNY';

-- ---------- biz_withdraw_fee_patch.sql ----------
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

-- ---------- biz_withdraw_need_kyc_patch.sql ----------
SET NAMES utf8mb4;
-- Withdraw requires KYC: default false (same as current, no check). Safe to re-run.

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'Withdraw need KYC', 'biz.withdraw.needKyc', 'false', 'N', 'admin', sysdate(), 'true = must finish KYC before withdraw'
from dual
where not exists (select 1 from sys_config where config_key = 'biz.withdraw.needKyc');

-- ---------- biz_pay_channel_patch.sql ----------
SET NAMES utf8mb4;
-- 线上代收：服务商 / 通道 / 支付单。当前全部 mock_mode=1，用模拟收银台跑通下单和回调入账。
-- 以后把 mock_mode 改成 0 并填真实网关，即可换成宝利/百付/牛付/沙付。

create table if not exists biz_pay_provider (
  provider_id       bigint(20)      not null auto_increment    comment '服务商ID',
  provider_code     varchar(32)     not null                   comment 'baifu/baoli/niupay/shapay/baoli_u',
  provider_name     varchar(64)     not null                   comment '显示名',
  adapter_family    varchar(32)     not null default 'monpay'  comment '适配器族 mock/monpay',
  gateway_url       varchar(255)    default ''                 comment '网关地址，模拟态可空',
  app_id            varchar(64)     default ''                 comment '商户号',
  secret_key        varchar(128)    default ''                 comment '签名密钥，模拟密钥不是生产密钥',
  mock_mode         char(1)         default '1'                comment '1模拟 0真实',
  status            char(1)         default '0'                comment '0正常 1停用',
  sort_order        int(4)          default 0                  comment '排序',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (provider_id),
  unique key uk_biz_pay_provider_code (provider_code)
) engine=innodb comment = '支付服务商';

create table if not exists biz_pay_channel (
  channel_id        bigint(20)      not null auto_increment    comment '通道ID',
  provider_code     varchar(32)     not null                   comment '服务商编码',
  channel_code      varchar(64)     not null                   comment '业务通道编码',
  channel_name      varchar(64)     not null                   comment '通道名',
  display_name      varchar(64)     default ''                 comment 'App展示名',
  scene             varchar(16)     not null                   comment 'alipay/wechat/union/usdt',
  product_id        varchar(32)     default ''                 comment '三方产品ID',
  currency          varchar(16)     not null default 'CNY'     comment '入账币种',
  min_amount        decimal(18,4)   not null default 10        comment '最小金额',
  max_amount        decimal(18,4)   default null               comment '最大金额，空不限',
  weight            int(4)          default 100                comment '同场景权重',
  status            char(1)         default '0'                comment '0正常 1停用',
  sort_order        int(4)          default 0                  comment '排序',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  primary key (channel_id),
  unique key uk_biz_pay_channel_code (channel_code),
  key idx_biz_pay_channel_scene (scene, status)
) engine=innodb comment = '支付通道';

create table if not exists biz_pay_order (
  pay_order_id      bigint(20)      not null auto_increment    comment '支付单ID',
  out_trade_no      varchar(64)     not null                   comment '商户订单号',
  recharge_id       bigint(20)      not null                   comment '充值单ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  provider_code     varchar(32)     not null                   comment '服务商',
  channel_code      varchar(64)     not null                   comment '通道',
  product_id        varchar(32)     default ''                 comment '三方产品ID',
  currency          varchar(16)     not null                   comment '币种',
  amount            decimal(18,4)   not null                   comment '入账金额',
  provider_amount   decimal(18,4)   not null                   comment '向三方下单金额',
  status            char(1)         default '0'                comment '0待付 1成功 2失败 3关闭',
  pay_type          varchar(16)     default 'url'              comment 'url/qr',
  pay_url           varchar(500)    default ''                 comment '支付跳转地址',
  provider_trade_no varchar(64)     default ''                 comment '三方单号',
  notify_payload    varchar(2000)   default ''                 comment '最近一次回调原文',
  expire_time       datetime                                   comment '过期时间',
  paid_time         datetime                                   comment '支付成功时间',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (pay_order_id),
  unique key uk_biz_pay_order_out (out_trade_no),
  key idx_biz_pay_order_member (member_id, status),
  key idx_biz_pay_order_recharge (recharge_id)
) engine=innodb comment = '线上支付单';

-- 充值单补线上字段（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_recharge_pay;
DELIMITER $$
CREATE PROCEDURE patch_biz_recharge_pay()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_recharge' AND COLUMN_NAME = 'pay_mode'
  ) THEN
    ALTER TABLE biz_recharge ADD COLUMN pay_mode char(1) default '0' comment '0人工 1线上' AFTER remark;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_recharge' AND COLUMN_NAME = 'channel_code'
  ) THEN
    ALTER TABLE biz_recharge ADD COLUMN channel_code varchar(64) default '' comment '支付通道' AFTER pay_mode;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_recharge' AND COLUMN_NAME = 'out_trade_no'
  ) THEN
    ALTER TABLE biz_recharge ADD COLUMN out_trade_no varchar(64) default '' comment '线上商户单号' AFTER channel_code;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_recharge_pay();
DROP PROCEDURE IF EXISTS patch_biz_recharge_pay;

insert into biz_pay_provider (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key, mock_mode, status, sort_order, remark, create_time)
select * from (
  select 'baifu' as provider_code, '百付' as provider_name, 'monpay' as adapter_family, 'https://mock.pay.local/baifu' as gateway_url, 'mock_baifu_app' as app_id, 'mock_baifu_secret' as secret_key, '1' as mock_mode, '0' as status, 1 as sort_order, '模拟：支付宝/微信/银联' as remark, sysdate() as create_time
  union all select 'baoli', '宝利', 'monpay', 'https://mock.pay.local/baoli', 'mock_baoli_app', 'mock_baoli_secret', '1', '0', 2, '模拟：支付宝/微信', sysdate()
  union all select 'niupay', '牛付', 'monpay', 'https://mock.pay.local/niupay', 'mock_niupay_app', 'mock_niupay_secret', '1', '0', 3, '模拟：支付宝/微信', sysdate()
  union all select 'shapay', '沙付', 'monpay', 'https://mock.pay.local/shapay', 'mock_shapay_app', 'mock_shapay_secret', '1', '0', 4, '模拟：支付宝/微信', sysdate()
  union all select 'baoli_u', '宝利U', 'monpay', 'https://mock.pay.local/baoli-u', 'mock_baoli_u_app', 'mock_baoli_u_secret', '1', '0', 5, '模拟：USDT代收', sysdate()
) t
where not exists (select 1 from biz_pay_provider p where p.provider_code = t.provider_code);

insert into biz_pay_channel (provider_code, channel_code, channel_name, display_name, scene, product_id, currency, min_amount, max_amount, weight, status, sort_order, create_time)
select * from (
  select 'baifu' as provider_code,'BAIFU_ALIPAY' as channel_code,'支付宝' as channel_name,'百付支付宝' as display_name,'alipay' as scene,'8801' as product_id,'CNY' as currency,10 as min_amount,50000 as max_amount,100 as weight,'0' as status,1 as sort_order,sysdate() as create_time
  union all select 'baifu','BAIFU_WECHAT','微信','百付微信','wechat','8802','CNY',10,50000,100,'0',2,sysdate()
  union all select 'baifu','BAIFU_UNION','银联快捷','百付银联','union','8808','CNY',10,50000,80,'0',3,sysdate()
  union all select 'baoli','BAOLI_ALIPAY','支付宝','宝利支付宝','alipay','8801','CNY',10,50000,90,'0',1,sysdate()
  union all select 'baoli','BAOLI_WECHAT','微信','宝利微信','wechat','8802','CNY',10,50000,90,'0',2,sysdate()
  union all select 'niupay','NIUPAY_ALIPAY','支付宝','牛付支付宝','alipay','8801','CNY',10,50000,80,'0',1,sysdate()
  union all select 'niupay','NIUPAY_WECHAT','微信','牛付微信','wechat','8802','CNY',10,50000,80,'0',2,sysdate()
  union all select 'shapay','SHAPAY_ALIPAY','支付宝','沙付支付宝','alipay','8801','CNY',10,50000,70,'0',1,sysdate()
  union all select 'shapay','SHAPAY_WECHAT','微信','沙付微信','wechat','8802','CNY',10,50000,70,'0',2,sysdate()
  union all select 'baoli_u','BAOLI_U_DEPOSIT','USDT代收','宝利U充值','usdt','30','USDT',10,20000,100,'0',1,sysdate()
) t
where not exists (select 1 from biz_pay_channel c where c.channel_code = t.channel_code);

delete from sys_role_menu where menu_id in (2028, 2029, 2307, 2308, 2309, 2310, 2311, 2312);
delete from sys_menu where menu_id in (2028, 2029, 2307, 2308, 2309, 2310, 2311, 2312);

insert into sys_menu values('2028', '支付通道', '2000', '15', 'payChannel', 'biz/payChannel/index', '', '', 1, 0, 'C', '0', '0', 'biz:payChannel:list', 'server', 'admin', sysdate(), '', null, '百付/宝利/牛付/沙付通道，当前为模拟');
insert into sys_menu values('2307', '通道查询', '2028', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payChannel:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2308', '通道修改', '2028', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payChannel:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2309', '服务商修改', '2028', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2029', '支付订单', '2000', '16', 'payOrder', 'biz/payOrder/index', '', '', 1, 0, 'C', '0', '0', 'biz:payOrder:list', 'list', 'admin', sysdate(), '', null, '线上代收单，模拟可点到账');
insert into sys_menu values('2310', '订单查询', '2029', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payOrder:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2311', '模拟到账', '2029', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payOrder:simulate', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2312', '服务商查询', '2028', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:list', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2028 as menu_id union all select 2029 union all select 2307 union all select 2308
  union all select 2309 union all select 2310 union all select 2311 union all select 2312
) m
where rm.menu_id = 2005
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

-- ---------- biz_app_support_patch.sql ----------
-- App 收款账户、客服中心、七级团队（可重复执行时加表用 if not exists）

create table if not exists biz_pay_account (
  account_id        bigint(20)      not null auto_increment    comment '账户ID',
  member_id         bigint(20)      not null                   comment '会员ID',
  account_type      varchar(20)     not null                   comment 'USDT/BANK/ALIPAY',
  account_name      varchar(100)    default ''                 comment '户名/实名',
  account_no        varchar(255)    not null                   comment '卡号/支付宝账号/USDT地址',
  bank_name         varchar(100)    default ''                 comment '银行名称',
  network           varchar(30)     default ''                 comment 'USDT网络 TRC20/ERC20',
  is_default        char(1)         default '0'                comment '是否默认 1是 0否',
  status            char(1)         default '0'                comment '0正常 1停用',
  create_time       datetime                                   comment '创建时间',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (account_id),
  key idx_pay_account_member (member_id, account_type)
) engine=innodb comment = '会员收款账户';

create table if not exists biz_cs_channel (
  channel_id        bigint(20)      not null auto_increment    comment '渠道ID',
  name              varchar(50)     not null                   comment '名称',
  channel_type      varchar(20)     default 'WECHAT'           comment 'PHONE/WECHAT/TELEGRAM/QQ/LINK/QR',
  value             varchar(255)    default ''                 comment '手机号/微信号/链接',
  qr_url            varchar(500)    default ''                 comment '二维码图片',
  link_url          varchar(500)    default ''                 comment '点击跳转',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '0显示 1隐藏',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (channel_id)
) engine=innodb comment = '客服渠道';

delete from sys_config where config_id between 48 and 50;
insert into sys_config values(48, '客服中心标题', 'biz.service.title', '客服中心', 'N', 'admin', sysdate(), '', null, 'App客服中心标题');
insert into sys_config values(49, '客服工作时间', 'biz.service.workTime', '09:00 - 21:00', 'N', 'admin', sysdate(), '', null, 'App客服工作时间');
insert into sys_config values(50, '客服提示文案', 'biz.service.hint', '通道拥堵可联系在线客服', 'N', 'admin', sysdate(), '', null, 'App客服说明');

insert into biz_cs_channel (name, channel_type, value, qr_url, link_url, sort, status, create_by, create_time, remark)
select '微信客服', 'WECHAT', '', '', '', 1, '0', 'admin', sysdate(), '请上传客服二维码或填写微信号'
from dual where not exists (select 1 from biz_cs_channel limit 1);

delete from sys_menu where menu_id in (2021, 2022, 2281, 2282, 2283, 2284, 2291, 2292, 2293, 2294);
insert into sys_menu values('2021', '收款账户', '2000', '11', 'payAccount', 'biz/payAccount/index', '', '', 1, 0, 'C', '0', '0', 'biz:payAccount:list', 'wallet', 'admin', sysdate(), '', null, '会员USDT/银行卡/支付宝收款账户');
insert into sys_menu values('2022', '客服中心', '2024', '1', 'service', 'biz/service/index', '', '', 1, 0, 'C', '0', '0', 'biz:service:list', 'service', 'admin', sysdate(), '', null, 'App联系客服渠道');
insert into sys_menu values('2281', '账户查询', '2021', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2282', '账户新增', '2021', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2283', '账户修改', '2021', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2284', '账户删除', '2021', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payAccount:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2291', '客服查询', '2022', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2292', '客服新增', '2022', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2293', '客服修改', '2022', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2294', '客服删除', '2022', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:service:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_team_structure_patch.sql ----------
SET NAMES utf8mb4;
-- 团队查询 / 会员结构图 / 推荐关系图（可重复执行）
-- 结构图：从某会员往下的直推树。关系图：从网体顶点到该会员的路径，列表为同级直推并标出路径上的人。

update sys_menu set menu_name = '团队查询', remark = '按会员查看1-7级下线与汇总' where menu_id = 2008;

delete from sys_role_menu where menu_id in (2030, 2031, 2313, 2314, 2315);
delete from sys_menu where menu_id in (2030, 2031, 2313, 2314, 2315);

insert into sys_menu values('2030', '会员结构图', '2000', '8', 'teamTree', 'biz/teamTree/index', '', '', 1, 0, 'C', '0', '0', 'biz:team:tree', 'tree-table', 'admin', sysdate(), '', null, '按手机号查看该会员下级树');
insert into sys_menu values('2313', '结构图查询', '2030', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:tree', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2031', '推荐关系图', '2000', '8', 'teamRelation', 'biz/teamRelation/index', '', '', 1, 0, 'C', '0', '0', 'biz:team:relation', 'nested', 'admin', sysdate(), '', null, '从顶点到该会员的推荐路径');
insert into sys_menu values('2314', '关系图查询', '2031', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:relation', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2315', '关系图导出', '2031', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:team:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2030 as menu_id union all select 2031 union all select 2313 union all select 2314 union all select 2315
) m
where rm.menu_id = 2008
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

-- ---------- biz_promo_rule_patch.sql ----------
-- 注册/实名/推广奖励规则（可重复执行：表已存在会报错可忽略）

create table if not exists biz_promo_grant (
  grant_id          bigint(20)      not null auto_increment    comment '发放ID',
  member_id         bigint(20)      not null                   comment '收款会员',
  from_member_id    bigint(20)      not null                   comment '来源会员：自领=本人，推广=被邀请人',
  grant_type        varchar(20)     not null                   comment 'KYC_SELF实名自领 INVITE推广奖励',
  currency          varchar(10)     default 'CNY'              comment '币种',
  amount            decimal(18,4)   default 0                  comment '金额',
  status            char(1)         default '1'                comment '1已发放',
  create_time       datetime                                   comment '创建时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (grant_id),
  unique key uk_promo_type_from (grant_type, from_member_id),
  key idx_promo_member (member_id)
) engine=innodb comment = '实名注册与推广奖励发放';

delete from sys_config where config_id between 61 and 74;
delete from sys_config where config_key in (
  'biz.promo.enabled','biz.promo.kycSelf.enabled','biz.promo.kycSelf.cny','biz.promo.kycSelf.usdt',
  'biz.promo.invite.enabled','biz.promo.invite.amount','biz.promo.invite.currency','biz.promo.invite.lockParent',
  'biz.team.enabled','biz.promo.ruleText'
);
insert into sys_config values(61, '推广规则总开关', 'biz.promo.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false关闭实名自领和邀请奖励');
insert into sys_config values(62, '实名注册奖励开关', 'biz.promo.kycSelf.enabled', 'true', 'N', 'admin', sysdate(), '', null, '实名后可选CNY或USDT领一次');
insert into sys_config values(63, '实名注册奖励CNY', 'biz.promo.kycSelf.cny', '14', 'N', 'admin', sysdate(), '', null, '实名注册奖励人民币金额');
insert into sys_config values(64, '实名注册奖励USDT', 'biz.promo.kycSelf.usdt', '2', 'N', 'admin', sysdate(), '', null, '实名注册奖励USDT金额');
insert into sys_config values(65, '实名推广奖励开关', 'biz.promo.invite.enabled', 'true', 'N', 'admin', sysdate(), '', null, '被邀请人实名后给邀请人发奖');
insert into sys_config values(66, '实名推广奖励金额', 'biz.promo.invite.amount', '2', 'N', 'admin', sysdate(), '', null, '每成功邀请1名实名用户的奖励');
insert into sys_config values(67, '实名推广奖励币种', 'biz.promo.invite.currency', 'CNY', 'N', 'admin', sysdate(), '', null, '邀请奖励币种 CNY或USDT');
insert into sys_config values(68, '邀请后不可改上级', 'biz.promo.invite.lockParent', 'true', 'N', 'admin', sysdate(), '', null, '注册时绑定邀请码后不可转移');
insert into sys_config values(69, '团队返佣开关', 'biz.team.enabled', 'true', 'N', 'admin', sysdate(), '', null, 'false关闭认购三级返佣');
insert into sys_config values(70, '注册推广规则说明', 'biz.promo.ruleText', '用户注册与推广奖励规则：
一、实名注册奖励
新用户完成注册并通过实名认证后，可获得 14 元或 2 USDT 平台余额，两种奖励方式任选其一。
二、实名推广奖励
每成功邀请 1 名新用户完成实名注册，邀请人可获得 2 元推广奖励。上下级不可以转移，请核对好正确的邀请码再注册。
三、团队返佣机制
一级返佣 9%、二级返佣 3%、三级返佣 1%

奖励资格、返佣计算及发放结果以平台系统实际核算为准；如发现异常注册、批量账户或其他违规行为，平台有权取消相关奖励资格。', 'N', 'admin', sysdate(), '', null, 'App邀请/规则页展示全文');

update sys_config set config_value = '2', remark = '每成功邀请1名实名用户给邀请人的金额' where config_key = 'biz.invite.reward';

delete from sys_menu where menu_id in (2023, 2295, 2296);
insert into sys_menu values('2023', '注册推广规则', '2000', '12', 'promo', 'biz/promo/index', '', '', 1, 0, 'C', '0', '0', 'biz:promo:query', 'peoples', 'admin', sysdate(), '', null, '实名注册奖励、邀请奖励与三级返佣');
insert into sys_menu values('2295', '推广规则查询', '2023', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:promo:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2296', '推广规则修改', '2023', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:promo:edit', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_kyc_reward_menu_patch.sql ----------
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

-- ---------- biz_blacklist_patch.sql ----------
SET NAMES utf8mb4;
-- 黑名单：姓名/手机/身份证/银行卡，拦截登录、注册、实名、绑卡，并记拦截记录

create table if not exists biz_blacklist (
  blacklist_id      bigint(20)      not null auto_increment    comment '黑名单ID',
  real_name         varchar(50)     default ''                 comment '姓名',
  phone             varchar(20)     default ''                 comment '手机号',
  id_card           varchar(32)     default ''                 comment '身份证号',
  bank_card         varchar(64)     default ''                 comment '银行卡号',
  status            char(1)         default '0'                comment '状态（0启用 1停用）',
  remark            varchar(500)    default ''                 comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (blacklist_id),
  key idx_biz_blacklist_phone (phone),
  key idx_biz_blacklist_id_card (id_card),
  key idx_biz_blacklist_bank (bank_card)
) engine=innodb comment = '黑名单';

create table if not exists biz_blacklist_log (
  log_id            bigint(20)      not null auto_increment    comment '记录ID',
  blacklist_id      bigint(20)      default null               comment '命中的黑名单ID',
  action            varchar(16)     not null                   comment 'LOGIN/REGISTER/KYC/BANK',
  hit_type          varchar(16)     not null                   comment 'PHONE/ID_CARD/BANK_CARD',
  hit_value         varchar(64)     default ''                 comment '命中值',
  member_id         bigint(20)      default null               comment '会员ID',
  phone             varchar(20)     default ''                 comment '当时手机号',
  real_name         varchar(50)     default ''                 comment '当时姓名',
  remark            varchar(500)    default ''                 comment '备注',
  create_time       datetime                                   comment '拦截时间',
  primary key (log_id),
  key idx_biz_blacklist_log_action (action),
  key idx_biz_blacklist_log_phone (phone),
  key idx_biz_blacklist_log_time (create_time)
) engine=innodb comment = '黑名单拦截记录';

delete from sys_role_menu where menu_id in (2026, 2027, 2301, 2302, 2303, 2304, 2305, 2306);
delete from sys_menu where menu_id in (2026, 2027, 2301, 2302, 2303, 2304, 2305, 2306);

insert into sys_menu values('2026', '黑名单', '2000', '13', 'blacklist', 'biz/blacklist/index', '', '', 1, 0, 'C', '0', '0', 'biz:blacklist:list', 'lock', 'admin', sysdate(), '', null, '拦截登录、注册、实名、绑卡');
insert into sys_menu values('2301', '黑名单查询', '2026', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2302', '黑名单新增', '2026', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2303', '黑名单修改', '2026', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2304', '黑名单删除', '2026', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklist:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2027', '黑名单记录', '2000', '14', 'blacklistLog', 'biz/blacklist/log', '', '', 1, 0, 'C', '0', '0', 'biz:blacklistLog:list', 'log', 'admin', sysdate(), '', null, '黑名单拦截记录');
insert into sys_menu values('2305', '记录查询', '2027', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklistLog:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2306', '记录删除', '2027', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:blacklistLog:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2026 as menu_id union all select 2027 union all select 2301 union all select 2302
  union all select 2303 union all select 2304 union all select 2305 union all select 2306
) m
where rm.menu_id = 2001
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

-- ---------- biz_member_login_log_patch.sql ----------
SET NAMES utf8mb4;
-- 会员登录日志（可重复执行）

create table if not exists biz_member_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  member_id      bigint(20)     default null              comment '会员ID',
  phone          varchar(20)    default ''                comment '手机号',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
  key idx_biz_member_login_phone (phone),
  key idx_biz_member_login_member (member_id),
  key idx_biz_member_login_s (status),
  key idx_biz_member_login_lt (login_time)
) engine=innodb comment = '会员登录日志';

delete from sys_role_menu where menu_id in (2034, 2330, 2331, 2332);
delete from sys_menu where menu_id in (2034, 2330, 2331, 2332);

insert into sys_menu values('2034', '会员登录日志', '2000', '2', 'memberLogin', 'biz/memberLogin/index', '', '', 1, 0, 'C', '0', '0', 'biz:memberLogin:list', 'logininfor', 'admin', sysdate(), '', null, 'App会员登录、注册、退出记录');
insert into sys_menu values('2330', '登录查询', '2034', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:memberLogin:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2331', '登录删除', '2034', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:memberLogin:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2332', '登录导出', '2034', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:memberLogin:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2034 as menu_id union all select 2330 union all select 2331 union all select 2332
) m
where rm.menu_id = 2001
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

-- ---------- biz_member_reset_pwd_patch.sql ----------
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

-- ---------- biz_fund_center_patch.sql ----------
SET NAMES utf8mb4;
-- 资金中心目录 + 支付供应商菜单（可重复执行）
-- 把充值/提现/流水/结构图/关系图/等级奖励/分佣/收款账户/通道/订单归到资金中心。

delete from sys_role_menu where menu_id in (2032, 2033, 2316, 2317, 2318);
delete from sys_menu where menu_id in (2032, 2033, 2316, 2317, 2318);

insert into sys_menu values('2032', '资金中心', '0', '6', 'fund', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, '充值提现、支付通道与资金记录');
update sys_menu set order_num = 7 where menu_id = 2025;
update sys_menu set order_num = 8 where menu_id = 2024;

update sys_menu set parent_id = 2032, order_num = 1 where menu_id = 2005;
update sys_menu set parent_id = 2032, order_num = 2 where menu_id = 2006;
update sys_menu set parent_id = 2032, order_num = 3 where menu_id = 2007;
update sys_menu set parent_id = 2032, order_num = 4 where menu_id = 2031;
update sys_menu set parent_id = 2032, order_num = 5 where menu_id = 2030;
update sys_menu set parent_id = 2032, order_num = 6 where menu_id = 2020;
update sys_menu set parent_id = 2032, order_num = 7 where menu_id = 2019;
update sys_menu set parent_id = 2032, order_num = 8 where menu_id = 2010;
update sys_menu set parent_id = 2032, order_num = 9 where menu_id = 2021;
insert into sys_menu values('2033', '供应商', '2032', '10', 'payProvider', 'biz/payProvider/index', '', '', 1, 0, 'C', '0', '0', 'biz:payProvider:list', 'server', 'admin', sysdate(), '', null, '代收服务商：百付/宝利/牛付/沙付，当前模拟');
insert into sys_menu values('2316', '供应商查询', '2033', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2317', '供应商修改', '2033', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2318', '供应商列表权限', '2033', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:payProvider:list', '#', 'admin', sysdate(), '', null, '');
update sys_menu set parent_id = 2032, order_num = 11 where menu_id = 2028;
update sys_menu set parent_id = 2032, order_num = 12 where menu_id = 2029;

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2032 as menu_id union all select 2033 union all select 2316 union all select 2317 union all select 2318
) m
where rm.menu_id in (2005, 2028)
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

-- ---------- biz_withdraw_wallet_patch.sql ----------
SET NAMES utf8mb4;
-- 提现规则可选扣款钱包，对应 App 产品收益 / 推广收益（可重复执行）

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PRODUCT', '产品收益提现', 'PRODUCT', '1', 9, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PRODUCT');

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PROMO', '推广收益提现', 'PROMO', '1', 10, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PROMO');

-- ---------- biz_wallet_adjust_patch.sql ----------
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

-- ---------- biz_wallet_log_operator_patch.sql ----------
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

-- ---------- biz_ops_config_page_patch.sql ----------
SET NAMES utf8mb4;
-- 业务参数改到对应功能页：补最高提现、把签到规则/注册推广挂回可见目录（可重复执行）

delete from sys_config where config_id in (71, 72) or config_key in ('biz.withdraw.maxAmount', 'biz.withdraw.maxAmount.usdt');
insert into sys_config values(71, '提现最高金额', 'biz.withdraw.maxAmount', '0', 'N', 'admin', sysdate(), '', null, '人民币最高提现，0表示不限');
insert into sys_config values(72, 'USDT最高提现', 'biz.withdraw.maxAmount.usdt', '0', 'N', 'admin', sysdate(), '', null, 'USDT最高提现，0表示不限');

-- 签到规则、签到中奖：产品交易
update sys_menu set parent_id = 2025, order_num = 5 where menu_id = 2011;
update sys_menu set parent_id = 2025, order_num = 6 where menu_id = 2012;

-- 注册推广规则：会员中心
update sys_menu set parent_id = 2297, order_num = 11 where menu_id = 2023;

-- ---------- biz_about_patch.sql ----------
-- App 关于我们（展示用，后台手改，可重复执行）
create table if not exists biz_about (
  about_id          bigint(20)      not null auto_increment    comment '内容ID',
  title             varchar(100)    not null                   comment '标题',
  subtitle          varchar(200)    default ''                 comment '副标题',
  content           mediumtext                                 comment '正文，后台富文本',
  image_url         varchar(500)    default ''                 comment '可选配图',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (about_id)
) engine=innodb comment = 'App关于我们';

insert into biz_about (title, subtitle, content, image_url, sort, status, create_by, create_time)
select '星帆智联', '连接星空 · 智联未来', '<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>', '', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_about limit 1);

delete from sys_menu where menu_id in (2014, 2211, 2212, 2213, 2214);
insert into sys_menu values('2014', '关于我们', '2024', '5', 'about', 'biz/about/index', '', '', 1, 0, 'C', '0', '0', 'biz:about:list', 'guide', 'admin', sysdate(), '', null, 'App关于我们，后台手改');
insert into sys_menu values('2211', '关于查询', '2014', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2212', '关于新增', '2014', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2213', '关于修改', '2014', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2214', '关于删除', '2014', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:about:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_about_singleton_patch.sql ----------
SET NAMES utf8mb4;
-- 关于我们改为全局一条：文本/PDF 二选一（可重复执行）

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_about'
    and column_name = 'display_mode'
);
set @sql := if(@exist = 0,
  'alter table biz_about add column display_mode varchar(16) not null default ''TEXT'' comment ''展示模式 TEXT/PDF'' after image_url',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_about'
    and column_name = 'pdf_url'
);
set @sql := if(@exist = 0,
  'alter table biz_about add column pdf_url varchar(500) default '''' comment ''PDF文件地址'' after display_mode',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_about set display_mode = 'TEXT' where display_mode is null or display_mode = '';
update biz_about set pdf_url = ifnull(pdf_url, '');

delete from biz_about
 where about_id not in (select id from (select min(about_id) as id from biz_about) t);

insert into biz_about (title, subtitle, content, image_url, display_mode, pdf_url, sort, status, create_by, create_time)
select '星帆智联', '连接星空 · 智联未来',
       '<p>星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。</p>',
       '', 'TEXT', '', 1, '0', 'admin', sysdate()
  from dual
 where not exists (select 1 from biz_about limit 1);

-- ---------- biz_news_patch.sql ----------
-- App 新闻资讯（展示用，后台手改，可重复执行）
create table if not exists biz_news (
  news_id           bigint(20)      not null auto_increment    comment '新闻ID',
  title             varchar(200)    not null                   comment '标题',
  summary           varchar(500)    default ''                 comment '摘要',
  cover_url         varchar(500)    default ''                 comment '封面图',
  content           mediumtext                                 comment '正文，后台富文本',
  publish_time      datetime                                   comment '发布日期',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (news_id)
) engine=innodb comment = 'App新闻资讯';

insert into biz_news (title, summary, cover_url, content, publish_time, sort, status, create_by, create_time)
select '俄罗斯近24小时遥感卫星观测任务与行业应用动态',
       '俄罗斯近24小时遥感卫星观测任务与行业应用动态',
       '',
       '<p>一、在轨遥感星座整体运行工况平稳</p><p>（一）高分辨率光学卫星完成农情、地质重点区域成像。</p><p>（二）雷达卫星持续开展全天候云雨覆盖区域观测。</p><p>二、行业应用动态</p><p>面向应急、农业、交通等场景的数据产品按计划分发，支撑多地业务系统稳定运行。</p>',
       '2026-08-18 00:00:00', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_news where title = '俄罗斯近24小时遥感卫星观测任务与行业应用动态');

insert into biz_news (title, summary, cover_url, content, publish_time, sort, status, create_by, create_time)
select '商业航天星座组网加速，行业应用场景持续拓展',
       '商业航天星座组网加速，行业应用场景持续拓展',
       '',
       '<p>商业航天正从单星验证走向规模组网。星帆智联持续推进星座部署与地面终端协同，为行业用户提供稳定连接能力。</p>',
       '2026-08-12 00:00:00', 2, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_news where title = '商业航天星座组网加速，行业应用场景持续拓展');

delete from sys_menu where menu_id in (2016, 2231, 2232, 2233, 2234);
insert into sys_menu values('2016', '新闻资讯', '2024', '3', 'news', 'biz/news/index', '', '', 1, 0, 'C', '0', '0', 'biz:news:list', 'documentation', 'admin', sysdate(), '', null, 'App新闻资讯，后台手改');
insert into sys_menu values('2231', '新闻查询', '2016', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2232', '新闻新增', '2016', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2233', '新闻修改', '2016', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2234', '新闻删除', '2016', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:news:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_group_chat_patch.sql ----------
-- App 官方群聊（展示用，后台上传二维码，可重复执行）
create table if not exists biz_group_chat (
  group_id          bigint(20)      not null auto_increment    comment '群聊ID',
  title             varchar(100)    not null                   comment '标题',
  hint              varchar(100)    default '扫码进群'          comment '二维码下方提示',
  qr_url            varchar(500)    default ''                 comment '群聊二维码图片地址',
  remark            varchar(500)    default ''                 comment '补充说明',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (group_id)
) engine=innodb comment = 'App官方群聊';

insert into biz_group_chat (title, hint, qr_url, remark, sort, status, create_by, create_time)
select '官方群聊', '扫码进群', '', '', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_group_chat limit 1);

delete from sys_menu where menu_id in (2015, 2221, 2222, 2223, 2224);
insert into sys_menu values('2015', '官方群聊', '2024', '4', 'groupChat', 'biz/groupChat/index', '', '', 1, 0, 'C', '0', '0', 'biz:group:list', 'message', 'admin', sysdate(), '', null, 'App官方群聊二维码，后台手改');
insert into sys_menu values('2221', '群聊查询', '2015', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2222', '群聊新增', '2015', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2223', '群聊修改', '2015', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2224', '群聊删除', '2015', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:group:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_carousel_patch.sql ----------
-- App 首页视频轮播（后台手改，可重复执行）
create table if not exists biz_carousel (
  carousel_id       bigint(20)      not null auto_increment    comment '轮播ID',
  title             varchar(100)    default ''                 comment '后台备注标题',
  video_url         varchar(500)    not null                   comment '视频地址',
  cover_url         varchar(500)    default ''                 comment '封面图，未播前展示',
  sort              int(4)          default 0                  comment '排序，越小越靠前',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (carousel_id)
) engine=innodb comment = 'App首页视频轮播';

delete from sys_menu where menu_id in (2018, 2251, 2252, 2253, 2254);
insert into sys_menu values('2018', '视频轮播', '2024', '2', 'carousel', 'biz/carousel/index', '', '', 1, 0, 'C', '0', '0', 'biz:carousel:list', 'example', 'admin', sysdate(), '', null, 'App首页视频轮播');
insert into sys_menu values('2251', '轮播查询', '2018', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2252', '轮播新增', '2018', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2253', '轮播修改', '2018', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2254', '轮播删除', '2018', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:carousel:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_overview_patch.sql ----------
-- App 首页运行概览（展示用，后台手改，可重复执行）
create table if not exists biz_overview (
  item_id           bigint(20)      not null auto_increment    comment '卡片ID',
  item_key          varchar(32)     not null                   comment '卡片标识，App用它匹配本地图',
  title             varchar(64)     not null                   comment '标题',
  display_value     varchar(64)     not null                   comment '展示数值，含单位',
  status_text       varchar(64)     default ''                 comment '状态文案',
  status_color      varchar(16)     default '#4DA3FF'          comment '状态点颜色',
  image_url         varchar(500)    default ''                 comment '可选配图，空则App用本地图',
  sort              int(4)          default 0                  comment '排序',
  status            char(1)         default '0'                comment '状态（0显示 1隐藏）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (item_id),
  unique key uk_biz_overview_key (item_key)
) engine=innodb comment = 'App运行概览';

insert into biz_overview (item_key, title, display_value, status_text, status_color, image_url, sort, status, create_by, create_time)
select 'satellite', '在轨卫星', '320 颗', '正常运行', '#3DDC84', '', 1, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_overview where item_key = 'satellite');

insert into biz_overview (item_key, title, display_value, status_text, status_color, image_url, sort, status, create_by, create_time)
select 'coverage', '覆盖国家/地区', '150+', '正常运行', '#4DA3FF', '', 2, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_overview where item_key = 'coverage');

insert into biz_overview (item_key, title, display_value, status_text, status_color, image_url, sort, status, create_by, create_time)
select 'terminal', '在线终端', '1256000+', '稳定连接', '#4DA3FF', '', 3, '0', 'admin', sysdate()
from dual where not exists (select 1 from biz_overview where item_key = 'terminal');

delete from sys_menu where menu_id in (2013, 2201, 2202, 2203, 2204);
insert into sys_menu values('2013', '运行概览', '2024', '6', 'overview', 'biz/overview/index', '', '', 1, 0, 'C', '0', '0', 'biz:overview:list', 'dashboard', 'admin', sysdate(), '', null, 'App首页展示数字，后台手改');
insert into sys_menu values('2201', '概览查询', '2013', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2202', '概览新增', '2013', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2203', '概览修改', '2013', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2204', '概览删除', '2013', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:overview:remove', '#', 'admin', sysdate(), '', null, '');

-- ---------- biz_content_menu_patch.sql ----------
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

-- ---------- biz_trade_menu_patch.sql ----------
SET NAMES utf8mb4;
-- 产品交易目录：产品分类、产品管理、认购订单、签到记录（可重复执行）
delete from sys_role_menu where menu_id = 2025;
delete from sys_menu where menu_id = 2025;
insert into sys_menu values('2025', '产品交易', '0', '6', 'trade', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '产品、认购与签到目录');
update sys_menu set order_num = 7 where menu_id = 2024;

update sys_menu set parent_id = 2025, order_num = 1 where menu_id = 2017;
update sys_menu set parent_id = 2025, order_num = 2 where menu_id = 2002;
update sys_menu set parent_id = 2025, order_num = 3 where menu_id = 2003;
update sys_menu set parent_id = 2025, order_num = 4 where menu_id = 2004;

insert into sys_role_menu (role_id, menu_id)
select distinct role_id, 2025 from sys_role_menu
where menu_id in (2017, 2002, 2003, 2004);

-- ---------- biz_menu_name_utf8_fix.sql ----------
SET NAMES utf8mb4;
update sys_menu set menu_name = '运营内容', remark = 'App展示与运营内容目录' where menu_id = 2024;
update sys_menu set menu_name = '产品交易', remark = '产品、认购与签到目录' where menu_id = 2025;

-- ---------- biz_member_center_menu_patch.sql ----------
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

-- ---------- biz_app_version_patch.sql ----------
SET NAMES utf8mb4;
-- App 版本管理。可重复执行。

create table if not exists biz_app_version (
  version_id        bigint(20)      not null auto_increment    comment '版本ID',
  platform          varchar(16)     not null                   comment 'android/ios',
  version           varchar(32)     not null                   comment '版本号，如 1.0.11',
  download_url      varchar(500)    not null                   comment '下载链接',
  description       varchar(1000)   default ''                 comment '版本说明',
  force_update      char(1)         not null default '0'       comment '强制更新 1是 0否',
  is_latest         char(1)         not null default '0'       comment '最新版本 1是 0否，同平台最多一条',
  is_enabled        char(1)         not null default '1'       comment '启用 1是 0否',
  sort_order        int(11)         not null default 0         comment '排序，越大越靠前',
  del_flag          char(1)         not null default '0'       comment '删除 0存在 1删除',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (version_id),
  key idx_biz_app_version_platform (platform, is_latest, is_enabled, del_flag)
) engine=innodb comment = 'App版本';

delete from sys_role_menu where menu_id in (2038, 2350, 2351, 2352, 2353, 2354);
delete from sys_menu where menu_id in (2038, 2350, 2351, 2352, 2353, 2354);

insert into sys_menu values('2038', '版本管理', '2024', '8', 'appVersion', 'biz/appVersion/index', '', '', 1, 0, 'C', '0', '0', 'biz:appVersion:list', 'guide', 'admin', sysdate(), '', null, 'App下载链接、强制更新、最新版本');
insert into sys_menu values('2350', '版本查询', '2038', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2351', '版本新增', '2038', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2352', '版本修改', '2038', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2353', '版本删除', '2038', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2354', '版本开关', '2038', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'biz:appVersion:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select distinct rm.role_id, m.menu_id
from sys_role_menu rm
join (
  select 2038 as menu_id union all select 2350 union all select 2351 union all select 2352 union all select 2353 union all select 2354
) m
where rm.menu_id = 2016
  and not exists (select 1 from sys_role_menu x where x.role_id = rm.role_id and x.menu_id = m.menu_id);

-- ---------- biz_member_test_flag_patch.sql ----------
SET NAMES utf8mb4;
-- 会员测试标记。打标后其数据不计入任何统计。可重复执行。

set @exist := (
  select count(*) from information_schema.columns
  where table_schema = database()
    and table_name = 'biz_member'
    and column_name = 'test_flag'
);
set @sql := if(@exist = 0,
  'alter table biz_member add column test_flag char(1) not null default ''0'' comment ''测试账号 0否 1是'' after status',
  'select 1');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

update biz_member set test_flag = '0' where test_flag is null;

SET FOREIGN_KEY_CHECKS = 1;

SELECT COUNT(*) AS table_count
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';
SELECT user_name, nick_name FROM sys_user WHERE user_id = 1;
SELECT menu_id, menu_name, parent_id FROM sys_menu WHERE menu_id IN (2024, 2025, 2032, 2038, 2297) ORDER BY menu_id;
SELECT COUNT(*) AS biz_config_count FROM sys_config WHERE config_key LIKE 'biz.%';
