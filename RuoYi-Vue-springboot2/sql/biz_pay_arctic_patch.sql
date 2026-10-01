SET NAMES utf8mb4;
-- Arctic Ocean deposit. Repeatable.
-- Fill channel.product_id with merchant "service" (channel code) before going live.

insert into biz_pay_provider (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key, callback_ips, mock_mode, status, sort_order, remark, create_time)
select 'arctic', 'ArcticOcean', 'arctic',
       'https://api.arcticocean.vip',
       '1790748705243954',
       'nqf0449kqdurisxfuy8flwj5wnlhuzfj',
       '52.74.17.206',
       '0', '0', 13,
       'form POST /pay/newOrder; amount yuan; product_id=service; notify reply ok',
       sysdate()
from dual
where not exists (select 1 from biz_pay_provider p where p.provider_code = 'arctic');

update biz_pay_provider
set provider_name = 'ArcticOcean',
    adapter_family = 'arctic',
    gateway_url = 'https://api.arcticocean.vip',
    app_id = '1790748705243954',
    secret_key = 'nqf0449kqdurisxfuy8flwj5wnlhuzfj',
    callback_ips = '52.74.17.206',
    mock_mode = '0',
    status = '0',
    remark = 'form POST /pay/newOrder; amount yuan; product_id=service; notify reply ok'
where provider_code = 'arctic';

insert into biz_pay_channel (provider_code, channel_code, channel_name, display_name, scene, product_id, currency, min_amount, max_amount, weight, status, sort_order, remark, create_time)
select * from (
  select 'arctic' as provider_code, 'ARCTIC_ALIPAY' as channel_code, 'Alipay' as channel_name, 'Alipay' as display_name,
         'alipay' as scene, 'payTest999' as product_id, 'CNY' as currency,
         10 as min_amount, 50000 as max_amount, 185 as weight, '0' as status, 1 as sort_order,
         'product_id = Arctic service; replace if they give real alipay code' as remark, sysdate() as create_time
  union all
  select 'arctic', 'ARCTIC_WECHAT', 'WeChat', 'WeChat', 'wechat', 'payTest999',
         'CNY', 10, 50000, 185, '0', 2, 'product_id = Arctic service; replace if they give real wechat code', sysdate()
) t
where not exists (select 1 from biz_pay_channel c where c.channel_code = t.channel_code);

update biz_pay_channel
set product_id = 'payTest999',
    remark = 'product_id = Arctic service payTest999'
where provider_code = 'arctic'
  and (product_id is null or product_id = '');
