SET NAMES utf8mb4;
-- Dashboard slow-query indexes. Repeatable. No business data change.

drop procedure if exists biz_patch_dashboard_index;

delimiter $$

create procedure biz_patch_dashboard_index()
begin
  if exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_member' and column_name = 'test_flag')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_member' and index_name = 'idx_biz_member_test_create') then
    alter table biz_member add key idx_biz_member_test_create (test_flag, create_time);
  end if;

  if exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_member' and column_name = 'test_flag')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_member' and index_name = 'idx_biz_member_test_kyc') then
    alter table biz_member add key idx_biz_member_test_kyc (test_flag, kyc_status);
  end if;

  if exists (select 1 from information_schema.tables where table_schema = database() and table_name = 'biz_checkin')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_checkin' and index_name = 'idx_biz_checkin_date') then
    alter table biz_checkin add key idx_biz_checkin_date (checkin_date);
  end if;

  if exists (select 1 from information_schema.tables where table_schema = database() and table_name = 'biz_recharge')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_recharge' and index_name = 'idx_biz_recharge_status_create') then
    alter table biz_recharge add key idx_biz_recharge_status_create (status, create_time);
  end if;

  if exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_recharge' and column_name = 'audit_time')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_recharge' and index_name = 'idx_biz_recharge_status_audit') then
    alter table biz_recharge add key idx_biz_recharge_status_audit (status, audit_time);
  end if;

  if exists (select 1 from information_schema.tables where table_schema = database() and table_name = 'biz_withdraw')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_withdraw' and index_name = 'idx_biz_withdraw_status_create') then
    alter table biz_withdraw add key idx_biz_withdraw_status_create (status, create_time);
  end if;

  if exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_withdraw' and column_name = 'audit_time')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_withdraw' and index_name = 'idx_biz_withdraw_status_audit') then
    alter table biz_withdraw add key idx_biz_withdraw_status_audit (status, audit_time);
  end if;

  if exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_withdraw' and column_name = 'wallet_type_code')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_withdraw' and index_name = 'idx_biz_withdraw_type_status') then
    alter table biz_withdraw add key idx_biz_withdraw_type_status (wallet_type_code, status);
  end if;

  if exists (select 1 from information_schema.tables where table_schema = database() and table_name = 'biz_order')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_order' and index_name = 'idx_biz_order_create') then
    alter table biz_order add key idx_biz_order_create (create_time);
  end if;

  if exists (select 1 from information_schema.columns where table_schema = database() and table_name = 'biz_wallet_log' and column_name = 'biz_type')
     and not exists (select 1 from information_schema.statistics where table_schema = database() and table_name = 'biz_wallet_log' and index_name = 'idx_biz_wallet_log_biz_time') then
    alter table biz_wallet_log add key idx_biz_wallet_log_biz_time (biz_type, create_time);
  end if;
end $$

delimiter ;

call biz_patch_dashboard_index();
drop procedure if exists biz_patch_dashboard_index;
