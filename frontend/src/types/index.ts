/** 登录请求参数 */
export interface LoginDTO {
  username: string
  password: string
}

/** 用户信息 */
export interface User {
  id?: number
  username: string
  password?: string
  realName: string
  /** 角色：ADMIN-管理员 CASHIER-收银员 */
  role: 'ADMIN' | 'CASHIER'
  phone: string
  /** 状态：0-禁用 1-启用 */
  status: number
}

/** 商品分类 */
export interface Category {
  id?: number
  name: string
  /** 排序号 */
  sort: number
  /** 状态：0-禁用 1-启用 */
  status: number
}

/** 商品 */
export interface Product {
  id?: number
  name: string
  /** 条形码 */
  barcode: string
  /** 分类ID */
  categoryId: number
  /** 分类名称（查询时返回） */
  categoryName?: string
  /** 售价 */
  price: number
  /** 成本价 */
  costPrice: number
  /** 库存数量 */
  stock: number
  /** 预警库存 */
  warningStock: number
  /** 单位 */
  unit: string
  /** 商品图片 */
  image: string
  /** 状态：0-下架 1-上架 */
  status: number
}

/** 订单 */
export interface Order {
  id: number
  /** 订单编号 */
  orderNo: string
  /** 订单总额 */
  totalAmount: number
  /** 优惠金额 */
  discountAmount: number
  /** 实付金额 */
  payAmount: number
  /** 成本金额 */
  costAmount: number
  /** 支付方式：CASH-现金 ALIPAY-支付宝 WECHAT-微信 */
  payType: string
  /** 状态：PENDING-待支付 PAID-已支付 REFUNDING-退款中 REFUNDED-已退款 CANCELED-已取消 */
  status: string
  /** 会员ID */
  memberId?: number
  /** 会员名称（查询时返回） */
  memberName?: string
  /** 收银员ID */
  cashierId?: number
  /** 收银员名称（查询时返回） */
  cashierName?: string
  /** 备注 */
  remark?: string
  /** 创建时间 */
  createTime: string
  /** 订单明细 */
  items?: OrderItem[]
}

/** 订单明细 */
export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  price: number
  quantity: number
  subtotal: number
}

/** 创建订单请求参数 */
export interface OrderDTO {
  /** 商品列表 */
  items: Array<{
    productId: number
    productName: string
    quantity: number
    price: number
  }>
  /** 会员ID（可选） */
  memberId?: number
  /** 支付方式：CASH-现金 ALIPAY-支付宝 WECHAT-微信 */
  payType: string
  /** 备注（可选） */
  remark?: string
}

/** 会员 */
export interface Member {
  id?: number
  name: string
  phone: string
  /** 积分 */
  points: number
  /** 累计消费金额 */
  totalSpent?: number
  /** 会员等级 */
  level: string
}

/** 会员等级配置 */
export interface MemberLevel {
  id?: number
  name: string
  /** 升级门槛 */
  minSpent: number
  /** 折扣比例 */
  discountRate: number
  /** 积分倍率 */
  pointsMultiplier: number
  /** 排序 */
  sort: number
  /** 状态：0-禁用，1-启用 */
  status: number
}

/** 供应商 */
export interface Supplier {
  id?: number
  name: string
  contactPerson: string
  phone: string
  address: string
  remark: string
  status: number
  createTime?: string
  updateTime?: string
}

/** 采购订单 */
export interface PurchaseOrder {
  id?: number
  orderNo: string
  supplierId: number
  supplierName?: string
  totalAmount: number
  remark: string
  status: number
  createTime: string
  updateTime?: string
  items?: PurchaseOrderItem[]
}

/** 采购订单明细 */
export interface PurchaseOrderItem {
  id?: number
  purchaseOrderId?: number
  productId: number
  productName: string
  quantity: number
  purchasePrice: number
  subtotal?: number
}

/** 操作日志 */
export interface OperationLog {
  id?: number
  userId: number
  userName: string
  module: string
  action: string
  targetId: number
  detail: string
  createTime: string
}

/** 库存盘点单 */
export interface InventoryCount {
  id?: number
  countNo: string
  countDate: string
  status: number
  remark: string
  createTime: string
  updateTime?: string
  items?: InventoryCountItem[]
}

/** 盘点明细 */
export interface InventoryCountItem {
  id?: number
  countId?: number
  productId: number
  productName: string
  bookStock: number
  actualStock: number
  difference: number
  remark: string
}

/** 促销活动 */
export interface Promotion {
  id?: number
  name: string
  type: string
  startTime: string
  endTime: string
  ruleConfig: string
  remark: string
  status: number
  createTime?: string
  updateTime?: string
}

/** API 统一响应格式 */
export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

/** 分页结果 */
export interface PageResult<T> {
  total: number
  page: number
  pageSize: number
  list: T[]
}

/** 每日营收统计 */
export interface DailyRevenue {
  date: string
  revenue: number
  cost: number
  profit: number
  orderCount: number
}

/** 每月营收统计 */
export interface MonthlyRevenue {
  year: number
  month: number
  revenue: number
  cost: number
  profit: number
}

/** 热销商品排行 */
export interface TopProduct {
  productName: string
  totalQuantity: number
  totalAmount: number
}