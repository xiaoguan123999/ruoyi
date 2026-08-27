SET NAMES utf8mb4;
-- 注册无默认等级：level_id 允许为空（可重复执行）
-- 不要把新会员写成 1：别的环境 id=1 可能是真实等级
alter table biz_member modify column level_id bigint(20) default null comment '会员等级ID，空表示无等级';

-- 只清掉指向已不存在等级的脏数据；id=1 若在 biz_level 里真实存在则不改
update biz_member m
left join biz_level l on l.level_id = m.level_id
set m.level_id = null
where m.level_id is not null and l.level_id is null;
