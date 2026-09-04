import request from '@/utils/request'
import type { AjaxResult, TableDataInfo } from '@/types'

export function listMember(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/member/list', method: 'get', params: query })
}

export function getMember(memberId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/member/' + memberId, method: 'get' })
}

export function addMember(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/member', method: 'post', data })
}

export function updateMember(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/member', method: 'put', data })
}

export function resetMemberGoogle(memberId: number): Promise<AjaxResult> {
  return request({ url: '/biz/member/' + memberId + '/google/reset', method: 'put' })
}

export function getMemberGoogleConfig(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/member/googleConfig', method: 'get' })
}

export function saveMemberGoogleConfig(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/member/googleConfig', method: 'put', data })
}

export function resetMemberPwd(memberId: number, password: string): Promise<AjaxResult> {
  return request({ url: '/biz/member/resetPwd', method: 'put', data: { memberId, password } })
}

export function resetMemberPayPwd(memberId: number, payPassword: string): Promise<AjaxResult> {
  return request({ url: '/biz/member/resetPayPwd', method: 'put', data: { memberId, payPassword } })
}

export function listMemberTeam(memberId: number, teamLevel?: number): Promise<AjaxResult<any[]>> {
  return request({ url: '/biz/member/team/' + memberId, method: 'get', params: { teamLevel } })
}

export function listProduct(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/product/list', method: 'get', params: query })
}

export function getProduct(productId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/product/' + productId, method: 'get' })
}

export function addProduct(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/product', method: 'post', data })
}

export function updateProduct(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/product', method: 'put', data })
}

export function delProduct(productId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/product/' + productId, method: 'delete' })
}

export function listProductCategory(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/productCategory/list', method: 'get', params: query })
}

export function listProductCategoryOptions(): Promise<AjaxResult<any[]>> {
  return request({ url: '/biz/productCategory/options', method: 'get' })
}

export function getProductCategory(categoryId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/productCategory/' + categoryId, method: 'get' })
}

export function addProductCategory(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/productCategory', method: 'post', data })
}

export function updateProductCategory(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/productCategory', method: 'put', data })
}

export function delProductCategory(categoryId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/productCategory/' + categoryId, method: 'delete' })
}

export function listOrder(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/order/list', method: 'get', params: query })
}

export function listCheckin(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/checkin/list', method: 'get', params: query })
}

export function listRecharge(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/recharge/list', method: 'get', params: query })
}

export function addRecharge(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/recharge', method: 'post', data })
}

export function auditRecharge(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/recharge/audit', method: 'put', data })
}

export function listWithdraw(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/withdraw/list', method: 'get', params: query })
}

export function getWithdrawConfig(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/withdraw/config', method: 'get' })
}

export function saveWithdrawConfig(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/withdraw/config', method: 'put', data })
}

export function auditWithdraw(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/withdraw/audit', method: 'put', data })
}

export function auditWithdrawBatch(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/withdraw/audit/batch', method: 'put', data })
}

export function listWalletLog(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/walletLog/list', method: 'get', params: query })
}

export function getWalletBalance(params: {
  memberId: number
  typeCode?: string
  currency: string
}): Promise<AjaxResult<any>> {
  return request({ url: '/biz/walletLog/balance', method: 'get', params })
}

export function adjustWallet(data: {
  memberId: number
  typeCode?: string
  currency: string
  direction: string
  amount: number | string
  remark: string
}): Promise<AjaxResult> {
  return request({ url: '/biz/walletLog/adjust', method: 'put', data })
}

export function listWalletType(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/walletType/list', method: 'get', params: query })
}

export function listWalletTypeOptions(): Promise<AjaxResult<any[]>> {
  return request({ url: '/biz/walletType/options', method: 'get' })
}

export function getWalletType(typeId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/walletType/' + typeId, method: 'get' })
}

export function addWalletType(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/walletType', method: 'post', data })
}

export function updateWalletType(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/walletType', method: 'put', data })
}

export function delWalletType(typeId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/walletType/' + typeId, method: 'delete' })
}

export function listWalletCredit(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/walletCredit/list', method: 'get', params: query })
}

export function getWalletCredit(ruleId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/walletCredit/' + ruleId, method: 'get' })
}

export function addWalletCredit(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/walletCredit', method: 'post', data })
}

export function updateWalletCredit(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/walletCredit', method: 'put', data })
}

export function delWalletCredit(ruleId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/walletCredit/' + ruleId, method: 'delete' })
}

export function getWalletCreditByBiz(bizType: string): Promise<AjaxResult<any>> {
  return request({ url: '/biz/walletCredit/byBiz/' + bizType, method: 'get' })
}

export function saveWalletCreditByBiz(bizType: string, typeCode: string): Promise<AjaxResult> {
  return request({ url: '/biz/walletCredit/byBiz/' + bizType, method: 'put', data: { typeCode } })
}

export function listTeam(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/team/list', method: 'get', params: query })
}

export function getTeamSummary(memberId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/team/summary/' + memberId, method: 'get' })
}

export function getTeamTree(keyword: string): Promise<AjaxResult<any>> {
  return request({ url: '/biz/team/tree', method: 'get', params: { keyword } })
}

export function listTeamChildren(memberId: number): Promise<AjaxResult<any[]>> {
  return request({ url: '/biz/team/children/' + memberId, method: 'get' })
}

export function listTeamRelation(keyword: string): Promise<AjaxResult<any[]>> {
  return request({ url: '/biz/team/relation', method: 'get', params: { keyword } })
}

export function listLevel(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/level/list', method: 'get', params: query })
}

export function getLevel(levelId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/level/' + levelId, method: 'get' })
}

export function addLevel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/level', method: 'post', data })
}

export function updateLevel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/level', method: 'put', data })
}

export function delLevel(levelId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/level/' + levelId, method: 'delete' })
}

export function getLevelRewardRule(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/levelReward/rule', method: 'get' })
}

export function saveLevelRewardRule(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/levelReward/rule', method: 'put', data })
}

export function listLevelRewardFxLog(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/levelReward/fxLog/list', method: 'get', params: query })
}

export function listLevelRewardLevel(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/levelReward/level/list', method: 'get', params: query })
}

export function updateLevelRewardLevel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/levelReward/level', method: 'put', data })
}

export function evaluateLevelReward(): Promise<AjaxResult> {
  return request({ url: '/biz/levelReward/evaluate', method: 'post' })
}

export function listLevelRewardGrant(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/levelReward/grant/list', method: 'get', params: query })
}

export function payLevelRewardGrant(grantId: number, data?: any): Promise<AjaxResult> {
  return request({ url: '/biz/levelReward/grant/pay/' + grantId, method: 'put', data })
}

export function rejectLevelRewardGrant(grantId: number, data?: any): Promise<AjaxResult> {
  return request({ url: '/biz/levelReward/grant/reject/' + grantId, method: 'put', data })
}

export function extraPayLevelReward(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/levelReward/grant/extraPay', method: 'post', data })
}

export function listCommission(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/commission/list', method: 'get', params: query })
}

export function getCheckinRule(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/checkin/rule', method: 'get' })
}

export function saveCheckinRule(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/checkin/rule', method: 'put', data })
}

export function listCheckinPrize(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/checkin/prize/list', method: 'get', params: query })
}

export function listOverview(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/overview/list', method: 'get', params: query })
}

export function getOverview(itemId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/overview/' + itemId, method: 'get' })
}

export function addOverview(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/overview', method: 'post', data })
}

export function updateOverview(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/overview', method: 'put', data })
}

export function delOverview(itemId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/overview/' + itemId, method: 'delete' })
}

export function getAboutConfig(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/about', method: 'get' })
}

export function saveAboutConfig(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/about', method: 'put', data })
}

export function listGroupChat(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/group/list', method: 'get', params: query })
}

export function getGroupChat(groupId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/group/' + groupId, method: 'get' })
}

export function addGroupChat(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/group', method: 'post', data })
}

export function updateGroupChat(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/group', method: 'put', data })
}

export function delGroupChat(groupId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/group/' + groupId, method: 'delete' })
}

export function listNews(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/news/list', method: 'get', params: query })
}

export function getNews(newsId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/news/' + newsId, method: 'get' })
}

export function addNews(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/news', method: 'post', data })
}

export function updateNews(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/news', method: 'put', data })
}

export function delNews(newsId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/news/' + newsId, method: 'delete' })
}

export function listCarousel(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/carousel/list', method: 'get', params: query })
}

export function getCarousel(carouselId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/carousel/' + carouselId, method: 'get' })
}

export function addCarousel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/carousel', method: 'post', data })
}

export function updateCarousel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/carousel', method: 'put', data })
}

export function delCarousel(carouselId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/carousel/' + carouselId, method: 'delete' })
}

export function listPayAccount(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/payAccount/list', method: 'get', params: query })
}

export function getPayAccount(accountId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/payAccount/' + accountId, method: 'get' })
}

export function addPayAccount(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/payAccount', method: 'post', data })
}

export function updatePayAccount(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/payAccount', method: 'put', data })
}

export function delPayAccount(accountId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/payAccount/' + accountId, method: 'delete' })
}

export function getCsConfig(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/service/config', method: 'get' })
}

export function saveCsConfig(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/service/config', method: 'put', data })
}

export function listCsChannel(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/service/list', method: 'get', params: query })
}

export function getCsChannel(channelId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/service/' + channelId, method: 'get' })
}

export function addCsChannel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/service', method: 'post', data })
}

export function updateCsChannel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/service', method: 'put', data })
}

export function delCsChannel(channelId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/service/' + channelId, method: 'delete' })
}

export function getPromoRule(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/promo/rule', method: 'get' })
}

export function savePromoRule(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/promo/rule', method: 'put', data })
}

export function listPromoGrant(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/promo/grant/list', method: 'get', params: query })
}

export function getKycRewardConfig(): Promise<AjaxResult<any>> {
  return request({ url: '/biz/kycReward', method: 'get' })
}

export function saveKycRewardConfig(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/kycReward', method: 'put', data })
}

export function listKycRewardGrant(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/kycReward/grant/list', method: 'get', params: query })
}

export function listBlacklist(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/blacklist/list', method: 'get', params: query })
}

export function getBlacklist(blacklistId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/blacklist/' + blacklistId, method: 'get' })
}

export function addBlacklist(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/blacklist', method: 'post', data })
}

export function updateBlacklist(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/blacklist', method: 'put', data })
}

export function delBlacklist(blacklistId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/blacklist/' + blacklistId, method: 'delete' })
}

export function listMemberLogin(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/memberLogin/list', method: 'get', params: query })
}

export function delMemberLogin(infoId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/memberLogin/' + infoId, method: 'delete' })
}

export function cleanMemberLogin(): Promise<AjaxResult> {
  return request({ url: '/biz/memberLogin/clean', method: 'delete' })
}

export function listBlacklistLog(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/blacklist/log/list', method: 'get', params: query })
}

export function delBlacklistLog(logId: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/blacklist/log/' + logId, method: 'delete' })
}

export function getDashboardStats(date?: string): Promise<AjaxResult<any>> {
  return request({ url: '/biz/dashboard/stats', method: 'get', params: { date }, timeout: 20000 })
}

export function getDashboardTrend(date?: string): Promise<AjaxResult<any>> {
  return request({ url: '/biz/dashboard/trend', method: 'get', params: { date }, timeout: 20000 })
}

export function listPayChannel(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/payChannel/list', method: 'get', params: query })
}

export function getPayChannel(channelId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/payChannel/' + channelId, method: 'get' })
}

export function updatePayChannel(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/payChannel', method: 'put', data })
}

export function listPayProvider(query?: any): Promise<AjaxResult<any[]>> {
  return request({ url: '/biz/payChannel/providers', method: 'get', params: query })
}

export function listPayProviderPage(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/payProvider/list', method: 'get', params: query })
}

export function getPayProvider(providerId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/payProvider/' + providerId, method: 'get' })
}

export function updatePayProvider(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/payProvider', method: 'put', data })
}

export function listPayOrder(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/payOrder/list', method: 'get', params: query })
}

export function simulatePayOrder(outTradeNo: string): Promise<AjaxResult> {
  return request({ url: '/biz/payOrder/simulate/' + outTradeNo, method: 'put' })
}

export function listAppVersion(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/appVersion/list', method: 'get', params: query })
}

export function getAppVersion(versionId: number): Promise<AjaxResult<any>> {
  return request({ url: '/biz/appVersion/' + versionId, method: 'get' })
}

export function addAppVersion(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/appVersion', method: 'post', data })
}

export function updateAppVersion(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/appVersion', method: 'put', data })
}

export function delAppVersion(versionIds: number | number[]): Promise<AjaxResult> {
  return request({ url: '/biz/appVersion/' + versionIds, method: 'delete' })
}

export function setAppVersionLatest(id: number, isLatest: boolean): Promise<AjaxResult> {
  return request({ url: '/biz/appVersion/' + id + '/latest', method: 'post', data: { isLatest } })
}

export function setAppVersionForceUpdate(id: number, forceUpdate: boolean): Promise<AjaxResult> {
  return request({ url: '/biz/appVersion/' + id + '/forceUpdate', method: 'post', data: { forceUpdate } })
}

export function setAppVersionEnabled(id: number, isEnabled: boolean): Promise<AjaxResult> {
  return request({ url: '/biz/appVersion/' + id + '/enabled', method: 'post', data: { isEnabled } })
}
