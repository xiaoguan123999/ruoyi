SET NAMES utf8mb4;
-- 提现规则可选扣款钱包，对应 App 产品收益 / 推广收益（可重复执行）

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PRODUCT', '产品收益提现', 'PRODUCT', '1', 9, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PRODUCT');

insert into biz_wallet_credit_rule (biz_type, biz_name, type_code, builtin, sort, create_by, create_time)
select 'WITHDRAW_PROMO', '推广收益提现', 'PROMO', '1', 10, 'admin', sysdate() from dual
where not exists (select 1 from biz_wallet_credit_rule where biz_type = 'WITHDRAW_PROMO');
