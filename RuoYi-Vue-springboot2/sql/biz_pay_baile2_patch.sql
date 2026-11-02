SET NAMES utf8mb4;
-- Baile v2 (baile.6666666.life). Repeatable.
-- channelType: wechat 1011, alipay 1012 (merchant pay-products).
-- Do not overwrite old baile (createorder).
-- payUrl from this gateway is a long JWT; varchar(500) truncates.

alter table biz_pay_order modify column pay_url varchar(2000) default '' comment 'pay url';

insert into biz_pay_provider (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key, callback_ips, mock_mode, status, sort_order, remark, create_time)
select 'baile2', 'Baile2', 'baile2',
       'https://baile.6666666.life',
       '10011',
       'f2f7f6848d6a3b19069f6a1287b3a1a8',
       '18.180.49.28',
       '0', '0', 15,
       'form POST /api/newOrder; amount yuan; product_id=channelType; notify reply ok',
       sysdate()
from dual
where not exists (select 1 from biz_pay_provider p where p.provider_code = 'baile2');

update biz_pay_provider
set provider_name = 'Baile2',
    adapter_family = 'baile2',
    gateway_url = 'https://baile.6666666.life',
    app_id = '10011',
    secret_key = 'f2f7f6848d6a3b19069f6a1287b3a1a8',
    callback_ips = '18.180.49.28',
    mock_mode = '0',
    status = '0',
    remark = 'form POST /api/newOrder; amount yuan; product_id=channelType; notify reply ok'
where provider_code = 'baile2';

insert into biz_pay_channel (provider_code, channel_code, channel_name, display_name, scene, product_id, currency, min_amount, max_amount, weight, status, sort_order, remark, create_time)
select * from (
  select 'baile2' as provider_code, 'BAILE2_ALIPAY' as channel_code, 'Alipay' as channel_name, 'Alipay' as display_name,
         'alipay' as scene, '1012' as product_id, 'CNY' as currency,
         100 as min_amount, 50000 as max_amount, 165 as weight, '0' as status, 1 as sort_order,
         'channelType 1012 Alipay' as remark, sysdate() as create_time
  union all
  select 'baile2', 'BAILE2_WECHAT', 'WeChat', 'WeChat', 'wechat', '1011',
         'CNY', 100, 50000, 165, '0', 2, 'channelType 1011 WeChat', sysdate()
) t
where not exists (select 1 from biz_pay_channel c where c.channel_code = t.channel_code);

update biz_pay_channel
set product_id = '1012', status = '0', remark = 'channelType 1012 Alipay'
where channel_code = 'BAILE2_ALIPAY';

update biz_pay_channel
set product_id = '1011', status = '0', remark = 'channelType 1011 WeChat'
where channel_code = 'BAILE2_WECHAT';
