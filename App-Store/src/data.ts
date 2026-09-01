/** 改这里就能增删应用、配置下载/跳转地址，不请求接口。
 *  应用图标放在 public/logos/{id}.png，和下面的 id 对应。
 */

export type Category = 'social' | 'entertainment' | 'tools' | 'finance'
export type Lang = 'zh' | 'en'

export const categoryOrder: Category[] = ['social', 'entertainment', 'tools', 'finance']

export const categoryLabel: Record<Category, Record<Lang, string>> = {
  social: { zh: '社交', en: 'Social' },
  entertainment: { zh: '娱乐', en: 'Entertainment' },
  tools: { zh: '工具', en: 'Tools' },
  finance: { zh: '金融', en: 'Finance' },
}

/** 填了哪个地址，页面就显示哪个按钮。没有的先别填。 */
export interface AppLinks {
  ios?: string
  android?: string
  web?: string
}

export interface AppItem {
  id: string
  name: Record<Lang, string>
  desc: Record<Lang, string>
  category: Category
  mark: string
  color: string
  version: string
  downloads: number
  links: AppLinks
  icon?: string
}

export const apps: AppItem[] = [
  {
    id: 'starsail',
    name: { zh: '星帆智联', en: 'StarSail' },
    desc: { zh: '以科技连接万物 · 让星辰触手可及', en: 'Technology that brings the stars within reach' },
    category: 'finance',
    mark: '星',
    color: '#0b62e8',
    version: '1.0.0',
    downloads: 0,
    links: {
      android: 'https://pub-33104bf8be274132a2d86c3749ec3d4b.r2.dev/preview/xfzl-1.0.0.apk',
      web: 'http://43.160.234.29:9527/',
    },
  },
  {
    id: 'wechat',
    name: { zh: '微信', en: 'WeChat' },
    desc: { zh: '聊天、支付、生活服务', en: 'Messaging and payments' },
    category: 'social',
    mark: '微',
    color: '#07C160',
    version: '8.0.56',
    downloads: 15000000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id414478124',
      android: 'https://weixin.qq.com',
      web: 'https://wx.qq.com',
    },
  },
  {
    id: 'qq',
    name: { zh: 'QQ', en: 'QQ' },
    desc: { zh: '即时通讯与兴趣社区', en: 'Messaging and communities' },
    category: 'social',
    mark: 'Q',
    color: '#12B7F5',
    version: '9.1.20',
    downloads: 9800000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id444934666',
      android: 'https://im.qq.com',
      web: 'https://im.qq.com',
    },
  },
  {
    id: 'dingtalk',
    name: { zh: '钉钉', en: 'DingTalk' },
    desc: { zh: '企业沟通与协作平台', en: 'Workplace messaging and collaboration' },
    category: 'social',
    mark: '钉',
    color: '#0089FF',
    version: '7.6.0',
    downloads: 4200000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id930368978',
      android: 'https://www.dingtalk.com/download',
      web: 'https://www.dingtalk.com',
    },
  },
  {
    id: 'wecom',
    name: { zh: '企业微信', en: 'WeCom' },
    desc: { zh: '连接微信的企业办公工具', en: 'Enterprise WeChat for work' },
    category: 'social',
    mark: '企',
    color: '#4584F7',
    version: '4.1.36',
    downloads: 3100000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id1108489592',
      android: 'https://work.weixin.qq.com/#indexDownload',
      web: 'https://work.weixin.qq.com',
    },
  },
  {
    id: 'douyin',
    name: { zh: '抖音', en: 'Douyin' },
    desc: { zh: '记录美好生活', en: 'Short videos' },
    category: 'entertainment',
    mark: '抖',
    color: '#111111',
    version: '32.1.0',
    downloads: 12000000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id1142110895',
      android: 'https://www.douyin.com/download',
      web: 'https://www.douyin.com',
    },
  },
  {
    id: 'bili',
    name: { zh: '哔哩哔哩', en: 'bilibili' },
    desc: { zh: '你感兴趣的视频都在 B 站', en: 'Videos and community' },
    category: 'entertainment',
    mark: 'B',
    color: '#FB7299',
    version: '8.20.0',
    downloads: 4300000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id736536022',
      android: 'https://app.bilibili.com',
      web: 'https://www.bilibili.com',
    },
  },
  {
    id: 'video',
    name: { zh: '腾讯视频', en: 'Tencent Video' },
    desc: { zh: '海量影视，会员畅看', en: 'Movies and shows' },
    category: 'entertainment',
    mark: '视',
    color: '#FF6699',
    version: '8.11.5',
    downloads: 5100000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id458318329',
      android: 'https://v.qq.com/download.html',
      web: 'https://v.qq.com',
    },
  },
  {
    id: 'netease-music',
    name: { zh: '网易云音乐', en: 'NetEase Music' },
    desc: { zh: '听见好时光', en: 'Music streaming' },
    category: 'entertainment',
    mark: '云',
    color: '#C20C0C',
    version: '9.1.40',
    downloads: 3900000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id590338362',
      android: 'https://music.163.com/download',
      web: 'https://music.163.com',
    },
  },
  {
    id: 'amap',
    name: { zh: '高德地图', en: 'Amap' },
    desc: { zh: '导航出行，到了再说', en: 'Maps and navigation' },
    category: 'tools',
    mark: '高',
    color: '#00B6FF',
    version: '15.4.0',
    downloads: 6400000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id461703208',
      android: 'https://mobile.amap.com',
      web: 'https://www.amap.com',
    },
  },
  {
    id: 'meeting',
    name: { zh: '腾讯会议', en: 'Tencent Meeting' },
    desc: { zh: '一键开会，高效协作', en: 'Online meetings' },
    category: 'tools',
    mark: '会',
    color: '#0066FF',
    version: '3.30.0',
    downloads: 2800000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id1484048379',
      android: 'https://meeting.tencent.com/download',
      web: 'https://meeting.tencent.com',
    },
  },
  {
    id: 'wps',
    name: { zh: 'WPS Office', en: 'WPS Office' },
    desc: { zh: '文档、表格、演示一站完成', en: 'Docs, sheets and slides' },
    category: 'tools',
    mark: 'W',
    color: '#D32F2F',
    version: '14.8.0',
    downloads: 7200000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id599852710',
      android: 'https://www.wps.cn/mobile',
      web: 'https://www.kdocs.cn',
    },
  },
  {
    id: 'taobao',
    name: { zh: '淘宝', en: 'Taobao' },
    desc: { zh: '淘你喜欢', en: 'Online shopping' },
    category: 'tools',
    mark: '淘',
    color: '#FF5000',
    version: '10.37.0',
    downloads: 8900000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id387682726',
      android: 'https://www.taobao.com',
      web: 'https://www.taobao.com',
    },
  },
  {
    id: 'jd',
    name: { zh: '京东', en: 'JD' },
    desc: { zh: '多快好省，购物上京东', en: 'Online shopping' },
    category: 'tools',
    mark: '京',
    color: '#E1251B',
    version: '13.2.8',
    downloads: 7200000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id414245413',
      android: 'https://app.jd.com',
      web: 'https://www.jd.com',
    },
  },
  {
    id: 'meituan',
    name: { zh: '美团', en: 'Meituan' },
    desc: { zh: '吃喝玩乐，美好生活', en: 'Food and local services' },
    category: 'tools',
    mark: '美',
    color: '#FFC300',
    version: '12.20.400',
    downloads: 8100000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id423084029',
      android: 'https://www.meituan.com',
      web: 'https://www.meituan.com',
    },
  },
  {
    id: 'didi',
    name: { zh: '滴滴出行', en: 'DiDi' },
    desc: { zh: '打车、顺风车、代驾', en: 'Ride hailing' },
    category: 'tools',
    mark: '滴',
    color: '#FF7D41',
    version: '6.8.4',
    downloads: 5600000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id554499054',
      android: 'https://www.didiglobal.com',
      web: 'https://www.didiglobal.com',
    },
  },
  {
    id: 'alipay',
    name: { zh: '支付宝', en: 'Alipay' },
    desc: { zh: '生活服务与支付平台', en: 'Payments and daily services' },
    category: 'finance',
    mark: '支',
    color: '#1677FF',
    version: '10.6.36',
    downloads: 12000000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id333206289',
      android: 'https://d.alipay.com',
      web: 'https://www.alipay.com',
    },
  },
  {
    id: 'unionpay',
    name: { zh: '云闪付', en: 'UnionPay' },
    desc: { zh: '银联官方支付应用', en: 'UnionPay mobile payments' },
    category: 'finance',
    mark: '云',
    color: '#E60012',
    version: '9.0.8',
    downloads: 2600000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id600273928',
      android: 'https://youhui.95516.com/hybrid_v4/html/help/download.html',
      web: 'https://www.unionpay.com',
    },
  },
  {
    id: 'icbc',
    name: { zh: '工商银行', en: 'ICBC' },
    desc: { zh: '手机银行，随时办理', en: 'Mobile banking' },
    category: 'finance',
    mark: '工',
    color: '#C7000B',
    version: '10.1.0',
    downloads: 3800000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id423168872',
      android: 'https://www.icbc.com.cn',
      web: 'https://www.icbc.com.cn',
    },
  },
  {
    id: 'cmb',
    name: { zh: '招商银行', en: 'CMB' },
    desc: { zh: '手机银行与理财服务', en: 'Mobile banking' },
    category: 'finance',
    mark: '招',
    color: '#E60012',
    version: '12.0.0',
    downloads: 2900000,
    links: {
      ios: 'https://apps.apple.com/cn/app/id392899562',
      android: 'https://www.cmbchina.com',
      web: 'https://www.cmbchina.com',
    },
  },
]

export function getApp(id: string) {
  return apps.find((app) => app.id === id)
}

export function formatDownloads(n: number, lang: Lang) {
  return `${n.toLocaleString('en-US')} ${lang === 'zh' ? '总下载' : 'downloads'}`
}
