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

export function auditWithdraw(data: any): Promise<AjaxResult> {
  return request({ url: '/biz/withdraw/audit', method: 'put', data })
}

export function listWalletLog(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/walletLog/list', method: 'get', params: query })
}

export function listTeam(query: any): Promise<TableDataInfo<any[]>> {
  return request({ url: '/biz/team/list', method: 'get', params: query })
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
