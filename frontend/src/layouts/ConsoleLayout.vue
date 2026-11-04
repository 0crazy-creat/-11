<template>
  <el-container class="console-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">CSP 管理后台</div>
      <el-menu :default-active="$route.path" router class="nav-menu">
        <el-menu-item index="/console"><el-icon><Odometer /></el-icon><span>控制台</span></el-menu-item>
        <el-menu-item v-if="can('contest:create')" index="/console/contests"><el-icon><Trophy /></el-icon><span>赛事管理</span></el-menu-item>
        <el-menu-item v-if="can('question:create')" index="/console/challenges"><el-icon><Flag /></el-icon><span>题库管理</span></el-menu-item>
        <el-menu-item v-if="can('team:manage')" index="/console/teams"><el-icon><User /></el-icon><span>战队管理</span></el-menu-item>
        <el-menu-item v-if="can('user:manage')" index="/console/users"><el-icon><Avatar /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item v-if="can('announcement:manage')" index="/console/announcements"><el-icon><Bell /></el-icon><span>公告管理</span></el-menu-item>
        <el-menu-item v-if="can('audit:view')" index="/console/audit"><el-icon><Document /></el-icon><span>审计日志</span></el-menu-item>
      </el-menu>
      <div class="aside-foot">
        <el-menu router class="nav-menu">
          <el-menu-item index="/portal"><el-icon><Back /></el-icon><span>返回选手端</span></el-menu-item>
        </el-menu>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="csp-h3 page-title">{{ $route.meta.title || '控制台' }}</span>
        <div class="right">
          <el-dropdown @command="onCommand">
            <span class="user csp-body">{{ user.profile?.nickname || user.profile?.username || '未登录' }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Avatar, Back, Bell, Document, Flag, Odometer, Trophy, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const user = useUserStore()
const perms = (() => {
  try {
    const raw = localStorage.getItem('csp_user') || localStorage.getItem('csp_profile')
    return raw ? (JSON.parse(raw).permissions || []) : []
  } catch (e) { return [] }
})()
const isAdmin = perms.includes('*')
function can(code) { return isAdmin || perms.includes(code) }

const router = useRouter()
onMounted(() => user.refresh())

function onCommand(cmd) {
  if (cmd === 'logout') {
    user.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.console-layout { height: 100vh; }

/* 侧栏：白底 + 右侧分割线，不做大面积深色/主色铺底 */
.aside {
  background: var(--csp-bg-page);
  border-right: 1px solid var(--csp-border);
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  line-height: 60px;
  padding-left: 20px;
  color: var(--csp-title);
  font-size: var(--csp-fs-h2);
  line-height: 60px;
  font-weight: var(--csp-fw-bold);
  border-bottom: 1px solid var(--csp-border);
}

/* 导航：常规态中性色，选中态主色高亮（主色唯一大面积出现的位置） */
.nav-menu {
  border-right: none;
  padding: 8px;
  --el-menu-item-height: 44px;
}
.nav-menu :deep(.el-menu-item) {
  height: 44px;
  margin-bottom: 4px;
  border-radius: var(--csp-radius-sm);
  font-size: var(--csp-fs-h3);
  color: var(--csp-text);
}
.nav-menu :deep(.el-menu-item:hover) {
  background: var(--csp-bg-card);
  color: var(--csp-title);
}
.nav-menu :deep(.el-menu-item.is-active) {
  background: var(--csp-primary-light);
  color: var(--csp-primary);
  font-weight: var(--csp-fw-bold);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--csp-bg-page);
  border-bottom: 1px solid var(--csp-border);
  height: 60px;
}
.page-title { color: var(--csp-title); }
.user { cursor: pointer; color: var(--csp-text); }
.user:hover { color: var(--csp-primary); }
.main { background: var(--csp-bg-card); padding: 20px; overflow: auto; }
</style>
