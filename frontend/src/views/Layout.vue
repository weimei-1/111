<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo" :class="{ 'logo-collapsed': isCollapse }">
        <div class="logo-icon">
          <el-icon :size="isCollapse ? 24 : 28"><ShoppingCart /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">超市订单管理</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        :collapse-transition="false"
        background-color="#ffffff"
        text-color="#333333"
        active-text-color="#ffffff"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>
            <span>{{ item.title }}</span>
          </template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer" v-show="!isCollapse">
        <div class="sidebar-user">
          <el-icon><User /></el-icon>
          <span>{{ userStore.userInfo?.realName || '未登录' }}</span>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="header-breadcrumb">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-tag
            :type="userStore.isAdmin ? 'danger' : 'warning'"
            size="small"
            class="role-tag"
          >
            <el-icon style="margin-right:4px;vertical-align:-2px">
              <StarFilled v-if="userStore.isAdmin" />
              <UserFilled v-else />
            </el-icon>
            {{ userStore.isAdmin ? '管理员' : '收银员' }}
          </el-tag>
          <el-dropdown @command="handleLogout">
            <span class="user-dropdown">
              <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
              <span class="user-name">{{ userStore.userInfo?.realName || '未登录' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  ShoppingCart, User, UserFilled, StarFilled,
  Fold, Expand, ArrowDown, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const toggleCollapse = (): void => {
  isCollapse.value = !isCollapse.value
}

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => {
  return route.meta?.title as string || ''
})

const menuItems = computed(() => {
  return router
    .getRoutes()
    .find((r) => r.path === '/')
    ?.children?.map((child) => ({
      path: child.path,
      title: child.meta?.title as string,
      icon: child.meta?.icon as string
    })) || []
})

const handleLogout = (command: string): void => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* ===== 侧边栏 ===== */
.layout-aside {
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border-right: 1px solid #e8e8e8;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1890ff, #096dd9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a2e;
  white-space: nowrap;
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid #e8e8e8;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8c8c8c;
  font-size: 13px;
}

/* 菜单样式 */
.layout-aside :deep(.el-menu) {
  border-right: none;
  flex: 1;
}

.layout-aside :deep(.el-menu-item) {
  margin: 2px 8px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
  transition: all 0.2s;
}

.layout-aside :deep(.el-menu-item:hover) {
  background-color: #e6f7ff !important;
  color: #1890ff !important;
}

.layout-aside :deep(.el-menu-item.is-active) {
  background-color: #096dd9 !important;
  color: #ffffff !important;
  box-shadow: 0 4px 12px rgba(9, 109, 217, 0.3);
}

/* ===== 顶栏 ===== */
.layout-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #8c8c8c;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #1890ff;
}

.header-breadcrumb {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.role-tag {
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #1890ff, #096dd9);
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* ===== 主内容 ===== */
.layout-main {
  background-color: #f0f2f5;
  padding: 20px 24px;
  overflow-y: auto;
}
</style>