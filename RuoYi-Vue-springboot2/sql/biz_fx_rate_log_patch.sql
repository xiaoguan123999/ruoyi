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
