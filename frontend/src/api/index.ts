import request from './request'
import type {
  LoginDTO,
  User,
  Category,
  Product,
  Order,
  OrderDTO,
  Member,
  MemberLevel,
  ApiResult,
  PageResult,
  DailyRevenue,
  MonthlyRevenue,
  TopProduct
} from '@/types'

// ==================== 认证管理 ====================

/** 登录 */
export const login = (data: LoginDTO): Promise<ApiResult<{ token: string; user: User }>> => {
  return request.post('/auth/login', data).then((res) => res.data)
}

// ==================== 用户管理 ====================

/** 获取用户列表 */
export const getUserList = (params?: Record<string, unknown>): Promise<ApiResult<PageResult<User>>> => {
  return request.get('/user/list', { params }).then((res) => res.data)
}

/** 新增用户 */
export const addUser = (data: User): Promise<ApiResult<null>> => {
  return request.post('/user/add', data).then((res) => res.data)
}

/** 更新用户 */
export const updateUser = (data: User): Promise<ApiResult<null>> => {
  return request.put('/user/update', data).then((res) => res.data)
}

/** 删除用户 */
export const deleteUser = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/user/${id}`).then((res) => res.data)
}

// ==================== 分类管理 ====================

/** 获取分类列表 */
export const getCategoryList = (params?: Record<string, unknown>): Promise<ApiResult<Category[]>> => {
  return request.get('/category/list', { params }).then((res) => res.data)
}

/** 新增分类 */
export const addCategory = (data: Category): Promise<ApiResult<null>> => {
  return request.post('/category/add', data).then((res) => res.data)
}

/** 更新分类 */
export const updateCategory = (data: Category): Promise<ApiResult<null>> => {
  return request.put('/category/update', data).then((res) => res.data)
}

/** 删除分类 */
export const deleteCategory = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/category/${id}`).then((res) => res.data)
}

// ==================== 商品管理 ====================

/** 获取商品列表（分页） */
export const getProductList = (params?: Record<string, unknown>): Promise<ApiResult<PageResult<Product>>> => {
  return request.get('/product/list', { params }).then((res) => res.data)
}

/** 新增商品 */
export const addProduct = (data: Product): Promise<ApiResult<null>> => {
  return request.post('/product', data).then((res) => res.data)
}

/** 更新商品 */
export const updateProduct = (data: Product): Promise<ApiResult<null>> => {
  return request.put('/product', data).then((res) => res.data)
}

/** 删除商品 */
export const deleteProduct = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/product/${id}`).then((res) => res.data)
}

/** 库存预警列表 */
export const getWarningList = (): Promise<ApiResult<Product[]>> => {
  return request.get('/product/warning').then((res) => res.data)
}

/** 根据条形码查询商品 */
export const getByBarcode = (barcode: string): Promise<ApiResult<Product>> => {
  return request.get(`/product/barcode/${barcode}`).then((res) => res.data)
}

// ==================== 订单管理 ====================

/** 创建订单 */
export const createOrder = (data: OrderDTO): Promise<ApiResult<Order>> => {
  return request.post('/order/create', data).then((res) => res.data)
}

/** 获取订单列表 */
export const getOrderList = (params?: Record<string, unknown>): Promise<ApiResult<PageResult<Order>>> => {
  return request.get('/order/list', { params }).then((res) => res.data)
}

/** 获取订单详情（后端返回 { order, items }，前端合并为 Order 对象） */
export const getOrderDetail = (id: number): Promise<ApiResult<Order>> => {
  return request.get(`/order/detail/${id}`).then((res) => {
    const raw = res.data
    if (raw.code === 200 && raw.data) {
      // 后端返回 { order, items }，合并为 flat Order
      raw.data = { ...raw.data.order, items: raw.data.items }
    }
    return raw
  })
}

/** 更新订单状态 */
export const updateOrderStatus = (data: { id: number; status: number }): Promise<ApiResult<null>> => {
  return request.put('/order/status', data).then((res) => res.data)
}

/** 退款订单 */
export const refundOrder = (id: number): Promise<ApiResult<null>> => {
  return request.post(`/order/refund/${id}`).then((res) => res.data)
}

/** 打印订单（后端返回 { printContent, order, items }） */
export const printOrder = (id: number): Promise<ApiResult<{ printContent: string }>> => {
  return request.get(`/order/print/${id}`).then((res) => res.data)
}

// ==================== 会员管理 ====================

/** 获取会员列表 */
export const getMemberList = (params?: Record<string, unknown>): Promise<ApiResult<PageResult<Member>>> => {
  return request.get('/member/list', { params }).then((res) => res.data)
}

/** 新增会员 */
export const addMember = (data: Member): Promise<ApiResult<null>> => {
  return request.post('/member/add', data).then((res) => res.data)
}

/** 更新会员 */
export const updateMember = (data: Member): Promise<ApiResult<null>> => {
  return request.put('/member/update', data).then((res) => res.data)
}

/** 删除会员 */
export const deleteMember = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/member/${id}`).then((res) => res.data)
}

/** 根据手机号查询会员 */
export const getByPhone = (phone: string): Promise<ApiResult<Member>> => {
  return request.get(`/member/phone/${phone}`).then((res) => res.data)
}

// ==================== 会员等级管理 ====================

/** 获取会员等级列表 */
export const getMemberLevelList = (params?: Record<string, unknown>): Promise<ApiResult<MemberLevel[]>> => {
  return request.get('/member-level/list', { params }).then((res) => res.data)
}

/** 新增会员等级 */
export const addMemberLevel = (data: MemberLevel): Promise<ApiResult<null>> => {
  return request.post('/member-level/add', data).then((res) => res.data)
}

/** 更新会员等级 */
export const updateMemberLevel = (data: MemberLevel): Promise<ApiResult<null>> => {
  return request.put('/member-level/update', data).then((res) => res.data)
}

/** 删除会员等级 */
export const deleteMemberLevel = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/member-level/${id}`).then((res) => res.data)
}

// ==================== 统计报表 ====================

/** 获取每日营收（后端返回 { date, revenue, cost, profit, orderCount }） */
export const getDailyRevenue = (date: string): Promise<ApiResult<DailyRevenue>> => {
  return request.get('/statistics/daily', { params: { date } }).then((res) => res.data)
}

/** 获取每月营收（后端返回 { year, month, revenue, cost, profit }） */
export const getMonthlyRevenue = (year: number, month: number): Promise<ApiResult<MonthlyRevenue>> => {
  return request.get('/statistics/monthly', { params: { year, month } }).then((res) => res.data)
}

/** 获取热销商品排行 */
export const getTopProducts = (params?: { date?: string; limit?: number }): Promise<ApiResult<TopProduct[]>> => {
  return request.get('/statistics/top-products', { params }).then((res) => res.data)
}

/** 获取采购统计（日采购额、月采购额） */
export const getPurchaseStats = (date: string): Promise<ApiResult<{ dailyPurchase: number; monthlyPurchase: number }>> => {
  return request.get('/statistics/purchase', { params: { date } }).then((res) => res.data)
}

// ==================== 供应商管理 ====================

/** 获取供应商列表 */
export const getSupplierList = (params?: Record<string, unknown>): Promise<ApiResult<Supplier[]>> => {
  return request.get('/supplier/list', { params }).then((res) => res.data)
}

/** 新增供应商 */
export const addSupplier = (data: Supplier): Promise<ApiResult<null>> => {
  return request.post('/supplier/add', data).then((res) => res.data)
}

/** 更新供应商 */
export const updateSupplier = (data: Supplier): Promise<ApiResult<null>> => {
  return request.put('/supplier/update', data).then((res) => res.data)
}

/** 删除供应商 */
export const deleteSupplier = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/supplier/${id}`).then((res) => res.data)
}

// ==================== 采购进货管理 ====================

/** 获取采购单列表（分页） */
export const getPurchaseList = (params?: Record<string, unknown>): Promise<ApiResult<PageResult<PurchaseOrder>>> => {
  return request.get('/purchase/list', { params }).then((res) => res.data)
}

/** 新增采购单 */
export const addPurchase = (data: PurchaseOrder): Promise<ApiResult<null>> => {
  return request.post('/purchase/add', data).then((res) => res.data)
}

/** 更新采购单 */
export const updatePurchase = (data: PurchaseOrder): Promise<ApiResult<null>> => {
  return request.put('/purchase/update', data).then((res) => res.data)
}

/** 删除采购单 */
export const deletePurchase = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/purchase/${id}`).then((res) => res.data)
}

/** 采购入库 */
export const purchaseStockIn = (id: number): Promise<ApiResult<null>> => {
  return request.put(`/purchase/stock-in/${id}`).then((res) => res.data)
}

// ==================== 库存盘点 ====================

/** 获取盘点单列表（分页） */
export const getInventoryCountList = (params?: Record<string, unknown>): Promise<ApiResult<PageResult<InventoryCount>>> => {
  return request.get('/inventory-count/list', { params }).then((res) => res.data)
}

/** 新增盘点单 */
export const addInventoryCount = (data: InventoryCount): Promise<ApiResult<null>> => {
  return request.post('/inventory-count/add', data).then((res) => res.data)
}

/** 完成盘点 */
export const completeInventoryCount = (id: number): Promise<ApiResult<null>> => {
  return request.put(`/inventory-count/complete/${id}`).then((res) => res.data)
}

/** 审核盘点 */
export const auditInventoryCount = (id: number): Promise<ApiResult<null>> => {
  return request.put(`/inventory-count/audit/${id}`).then((res) => res.data)
}

/** 删除盘点单 */
export const deleteInventoryCount = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/inventory-count/${id}`).then((res) => res.data)
}

// ==================== 促销活动管理 ====================

/** 获取促销活动列表 */
export const getPromotionList = (params?: Record<string, unknown>): Promise<ApiResult<Promotion[]>> => {
  return request.get('/promotion/list', { params }).then((res) => res.data)
}

/** 新增促销活动 */
export const addPromotion = (data: Promotion): Promise<ApiResult<null>> => {
  return request.post('/promotion/add', data).then((res) => res.data)
}

/** 更新促销活动 */
export const updatePromotion = (data: Promotion): Promise<ApiResult<null>> => {
  return request.put('/promotion/update', data).then((res) => res.data)
}

/** 删除促销活动 */
export const deletePromotion = (id: number): Promise<ApiResult<null>> => {
  return request.delete(`/promotion/${id}`).then((res) => res.data)
}

// ==================== 操作日志 ====================

/** 获取操作日志列表（分页） */
export const getOperationLogList = (params?: Record<string, unknown>): Promise<ApiResult<PageResult<OperationLog>>> => {
  return request.get('/operation-log/list', { params }).then((res) => res.data)
}

/** 批量删除操作日志 */
export const deleteOperationLog = (ids: number[]): Promise<ApiResult<null>> => {
  return request.delete('/operation-log/batch', { data: ids }).then((res) => res.data)
}