SET NAMES utf8mb4;

-- 福旺 / 无忧 / 非凡 服务商与通道
-- 文件请保持 UTF-8 保存/导入

-- 1) 表结构
ALTER TABLE biz_pay_provider
  MODIFY COLUMN secret_key varchar(256) DEFAULT '' COMMENT '签名密钥';

ALTER TABLE biz_pay_provider
  ADD COLUMN callback_ips varchar(255) DEFAULT '' COMMENT '回调来源IP，逗号分隔，空则不校验' AFTER secret_key;

-- 2) 服务商（按 provider_code 幂等）
INSERT INTO biz_pay_provider
  (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key,
   callback_ips, mock_mode, status, sort_order, remark, create_time)
SELECT * FROM (
  SELECT 'fuwang' AS provider_code, 'FuWang' AS provider_name, 'jeepay' AS adapter_family,
         'https://fuwang-pay.aaagood.xyz' AS gateway_url,
         'M2100946162876579840' AS app_id, '63d41a6ab1204846aa15cab2d423cf7b' AS secret_key,
         '' AS callback_ips, '0' AS mock_mode, '0' AS status, 10 AS sort_order,
         'FuWang: app_id=mchId; product_id=numeric wayCode' AS remark, NOW() AS create_time
  UNION ALL
  SELECT 'wuyou', 'WuYou', 'wuyou', 'http://pay.wyoukj.click',
         '10109', 'RGYNFXPAI7WX44GFFOALG4EY4U1WVQKHHSLOMWF17VHDOSZ7EYTIVEXSVUCVSBVCVRX4SOZ3HGSA9B4RTCSKNDMWVTXHIZPUYHINUZAECF8XAQPORGTYNIWB0PUS7KPM',
         '18.163.116.198', '0', '0', 11, 'product 8000 alipay 8001 wechat', NOW()
  UNION ALL
  SELECT 'feifan', 'FeiFan', 'monpay', 'https://nicepaymon-api.pangukaitianpidi.xyz',
         '494b481b65cb95f6c1233133', '0E7020b1143951764DA751CFbb7706F39e302Bed',
         '43.199.5.177', '0', '0', 12, 'monpay product 13 alipay 15 wechat', NOW()
) t
WHERE NOT EXISTS (SELECT 1 FROM biz_pay_provider x WHERE x.provider_code = t.provider_code);

-- 3) 通道（按 channel_code 幂等）
INSERT INTO biz_pay_channel
  (provider_code, channel_code, channel_name, display_name, scene, product_id,
   currency, min_amount, max_amount, weight, status, sort_order, remark, create_time)
SELECT * FROM (
  SELECT 'fuwang' AS provider_code, 'FUWANG_ALIPAY' AS channel_code, '支付宝' AS channel_name, '支付宝' AS display_name,
         'alipay' AS scene, '901' AS product_id, 'CNY' AS currency, 10 AS min_amount, 50000 AS max_amount,
         200 AS weight, '0' AS status, 1 AS sort_order, 'FuWang wayCode=901' AS remark, NOW() AS create_time
  UNION ALL SELECT 'fuwang','FUWANG_WECHAT','微信','微信','wechat','901','CNY',10,50000,200,'0',2,'FuWang wayCode=901',NOW()
  UNION ALL SELECT 'wuyou','WUYOU_ALIPAY','支付宝','支付宝','alipay','8000','CNY',100,20000,190,'0',1,'wuyou 8000',NOW()
  UNION ALL SELECT 'wuyou','WUYOU_WECHAT','微信','微信','wechat','8001','CNY',100,3000,190,'0',2,'wuyou 8001',NOW()
  UNION ALL SELECT 'feifan','FEIFAN_ALIPAY','支付宝','支付宝','alipay','13','CNY',100,50000,180,'0',1,'feifan 13',NOW()
  UNION ALL SELECT 'feifan','FEIFAN_WECHAT','微信','微信','wechat','15','CNY',100,50000,180,'0',2,'feifan 15',NOW()
) t
WHERE NOT EXISTS (SELECT 1 FROM biz_pay_channel x WHERE x.channel_code = t.channel_code);

-- 纠正历史乱码显示名
UPDATE biz_pay_channel SET channel_name='支付宝', display_name='支付宝'
WHERE channel_code IN ('FUWANG_ALIPAY','WUYOU_ALIPAY','FEIFAN_ALIPAY');
UPDATE biz_pay_channel SET channel_name='微信', display_name='微信'
WHERE channel_code IN ('FUWANG_WECHAT','WUYOU_WECHAT','FEIFAN_WECHAT');
