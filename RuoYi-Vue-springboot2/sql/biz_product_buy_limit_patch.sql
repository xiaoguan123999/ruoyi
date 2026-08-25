SET NAMES utf8mb4;
-- 产品限购：每人限购份数，0 表示不限制。列已存在会报 Duplicate column，可忽略。
alter table biz_product add column buy_limit int(11) default 0 comment '每人限购份数，0不限制' after withdraw_required;
update biz_product set buy_limit = 0 where buy_limit is null;
