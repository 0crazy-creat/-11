<template>
  <el-container class="portal-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">CSP 竞赛平台</div>
      <el-menu :default-active="$route.path" router class="nav-menu">
        <el-menu-item index="/portal"><el-icon><HomeFilled /></el-icon><span>赛事大厅</span></el-menu-item>
        <el-menu-item index="/portal/announcements"><el-icon><Bell /></el-icon><span>公告栏</span></el-menu-item>
        <el-menu-item index="/portal/awd"><el-icon><Monitor /></el-icon><span>AWD 比赛平台</span></el-menu-item>
        <el-menu-item index="/portal/practice"><el-icon><Reading /></el-icon><span>题目练习</span></el-menu-item>
        <el-menu-item index="/portal/ctf"><el-icon><Trophy /></el-icon><span>CTF 比赛</span></el-menu-item>
        <el-menu-item index="/portal/my-team"><el-icon><UserFilled /></el-icon><span>战队管理</span></el-menu-item>
        <el-menu-item v-if="showPlayerEntry" index="/portal/my"><el-icon><Tickets /></el-icon><span>我的比赛</span></el-menu-item>
        <el-menu-item v-if="role !== 'OBSERVER'" index="/portal/writeups"><el-icon><Notebook /></el-icon><span>题解社区</span></el-menu-item>
        <el-menu-item index="/portal/notifications"><el-icon><Message /></el-icon><span>消息通知</span></el-menu-item>
        <!-- 态势感知入口 -->
        <el-menu-item v-if="canViewScreen" index="/situation"><el-icon><DataLine /></el-icon><span>态势感知</span></el-menu-item>
      </el-menu>
      <div class="aside-foot">
        <el-menu :default-active="$route.path" router class="nav-menu">
          <el-menu-item v-if="user.isAdmin" index="/console"><el-icon><Setting /></el-icon><span>进入管理后台</span></el-menu-item>
          <el-menu-item v-else-if="canManage" index="/console/contests"><el-icon><Setting /></el-icon><span>赛事管理</span></el-menu-item>
        </el-menu>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="csp-h3 page-title">{{ $route.meta.title || '赛事大厅' }}</span>
        <div class="right">
          <NotificationBell />
          <el-tag v-if="role" size="small" effect="light" :type="roleTagType">{{ roleLabel }}</el-tag>
          <el-tag v-if="user.roles.length" size="small" effect="plain">{{ user.roles.join(' / ') }}</el-tag>
          <el-dropdown @command="onCommand">
            <span class="user csp-body">{{ user.profile?.nickname || user.profile?.username || '未登录' }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item v-if="user.isAdmin || canManage" command="console" divided>管理后台</el-dropdown-item>
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataLine, HomeFilled, Message, Monitor, Notebook, Reading, Setting, Tickets, Bell, Trophy, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { roleOf, canDo, roleLabelOf } from '@/constants/contestType'
import NotificationBell from '@/components/NotificationBell.vue'

const user = useUserStore()
const router = useRouter()

const role = computed(() => roleOf(user.roles))
const roleLabel = computed(() => roleLabelOf(role.value))
const roleTagType = computed(() => ({
  ADMIN: 'danger', SUPER_ADMIN: 'danger', PLATFORM_ADMIN: 'danger',
  JUDGE: 'warning', TEACHER: 'success', OBSERVER: 'info'
}[role.value] || ''))

/* 菜单按角色动态显示 */
const showPlayerEntry = computed(() => role.value !== 'OBSERVER')
const canReview = computed(() => canDo(role.value, 'review'))
const canManage = computed(() => canDo(role.value, 'manage'))
const canViewScreen = computed(() => ['ADMIN', 'SUPER_ADMIN', 'PLATFORM_ADMIN', 'JUDGE', 'OBSERVER'].includes(role.value))

function onCommand(c) {
  if (c === 'logout') {
    user.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (c === 'console') {
    router.push('/console')
  } else if (c === 'profile') {
    router.push('/portal/profile')
  }
}
</script>

<style scoped>
.portal-layout { height: 100vh; background: var(--csp-bg); }
.aside { background: #fff; border-right: 1px solid var(--csp-border); display: flex; flex-direction: column; }
.logo { height: 56px; display: flex; align-items: center; padding: 0 18px; font-weight: 600; color: var(--csp-primary); border-bottom: 1px solid var(--csp-border); }
.nav-menu { border-right: none; flex: 1; }
.aside-foot { border-top: 1px solid var(--csp-border); padding: 6px 0; }
.header { background: #fff; border-bottom: 1px solid var(--csp-border); display: flex; align-items: center; justify-content: space-between; height: 56px; }
.right { display: flex; align-items: center; gap: 14px; }
.user { cursor: pointer; color: var(--csp-title); }
.main { padding: 18px 20px; overflow-y: auto; }
.page-title { color: var(--csp-title); }
</style>