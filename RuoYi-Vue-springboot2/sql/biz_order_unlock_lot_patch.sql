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
