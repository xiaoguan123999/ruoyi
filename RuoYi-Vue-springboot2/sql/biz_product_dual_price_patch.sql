-- 同一产品同时配人民币/USDT 价格和日返。认购按所选币种扣对应钱包。
-- 加列若已存在会报错，可忽略后继续执行后面的 update。

alter table biz_product add column price_cny decimal(18,4) default null comment '人民币认购价' after price;
alter table biz_product add column price_usdt decimal(18,4) default null comment 'USDT认购价' after price_cny;
alter table biz_product add column daily_rebate_cny decimal(18,4) default null comment '人民币每日返利' after daily_rebate;
alter table biz_product add column daily_rebate_usdt decimal(18,4) default null comment 'USDT每日返利' after daily_rebate_cny;

update biz_product
   set price_cny = price, daily_rebate_cny = daily_rebate
 where price_cny is null
   and (currency is null or currency = '' or upper(currency) = 'CNY');

update biz_product
   set price_usdt = price, daily_rebate_usdt = daily_rebate
 where price_usdt is null
   and upper(currency) = 'USDT';
