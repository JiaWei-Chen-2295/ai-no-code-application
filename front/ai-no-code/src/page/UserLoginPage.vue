<template>
  <div id="userLoginPage">
    <div class="login-container">
      <div class="login-form">
        <div class="form-header">
          <Icon icon="mdi:application-brackets" class="form-logo-icon" />
          <h2 class="title">AppCraft</h2>
        </div>
        <div class="desc">
          <Icon icon="mdi:code-tags" class="desc-icon" />
          <span>不写一行代码，生成完整应用</span>
        </div>
        <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
          <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input v-model:value="formState.userAccount" placeholder="请输入账号">
              <template #prefix>
                <Icon icon="mdi:account-outline" class="input-icon" />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item name="userPassword" :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码不能小于 8 位' },
          ]">
            <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码">
              <template #prefix>
                <Icon icon="mdi:lock-outline" class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>
          <div class="tips">
            <Icon icon="mdi:information-outline" class="tips-icon" />
            <span>没有账号？</span>
            <RouterLink to="/user/register">去注册</RouterLink>
          </div>
          <a-form-item>
            <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
              <Icon icon="mdi:login" class="btn-icon" />
              <span>登录</span>
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userLogin } from '@/api/userController'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const loading = ref(false)
const userStore = useUserStore()

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserLoginRequest) => {
  loading.value = true
  try {
    const res = await userLogin(values)
    // 登录成功，跳转到首页
    if (res.data.code === 0) {
      message.success('登录成功')
      // 获取用户信息并保存到store
      await userStore.fetchLoginUser()
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error('登录失败，' + res.data.message)
    }
  } catch (error) {
    message.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#userLoginPage {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-600) 0%, var(--secondary-600) 50%, var(--deep-600) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-4);
  position: relative;
}

#userLoginPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255, 220, 0, 0.15), transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(144, 206, 66, 0.15), transparent 50%);
  pointer-events: none;
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: var(--spacing-5);
  z-index: 2;
  position: relative;
}

.login-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 2px solid var(--secondary-600);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-10);
  box-shadow: var(--shadow-2xl);
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

.login-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, var(--primary-600), var(--accent-500), var(--secondary-600));
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
}

.form-logo-icon {
  font-size: 36px;
  color: var(--primary-600);
}

.title {
  text-align: center;
  margin: 0;
  color: var(--deep-600);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  background: linear-gradient(135deg, var(--primary-600), var(--secondary-600));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.desc {
  text-align: center;
  color: var(--gray-600);
  margin-bottom: var(--spacing-6);
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
}

.desc-icon {
  font-size: 18px;
  color: var(--primary-500);
}

.tips {
  margin-bottom: var(--spacing-4);
  color: var(--gray-600);
  font-size: var(--text-sm);
  text-align: right;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--spacing-1);
}

.tips-icon {
  font-size: 16px;
  opacity: 0.7;
}

.input-icon {
  font-size: 18px;
  color: var(--gray-500);
}

.login-form :deep(.ant-btn-primary) .btn-icon {
  font-size: 18px;
  margin-right: var(--spacing-2);
}

.tips a {
  color: var(--primary-600);
  text-decoration: none;
  font-weight: var(--font-medium);
  transition: var(--transition-colors);
}

.tips a:hover {
  color: var(--accent-500);
}

/* 覆盖ant-design表单样式 */
.login-form :deep(.ant-input),
.login-form :deep(.ant-input-password) {
  border: 2px solid var(--secondary-200) !important;
  border-radius: var(--radius-lg) !important;
  padding: var(--spacing-3) var(--spacing-4) !important;
  font-size: var(--text-base) !important;
  transition: var(--transition-all) !important;
}

.login-form :deep(.ant-input:focus),
.login-form :deep(.ant-input-password-focused) {
  border-color: var(--accent-500) !important;
  box-shadow: 0 0 0 3px rgba(255, 220, 0, 0.2) !important;
}

.login-form :deep(.ant-btn-primary) {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-md) !important;
  transition: var(--transition-all) !important;
  height: 48px !important;
  font-size: var(--text-base) !important;
}

.login-form :deep(.ant-btn-primary:hover) {
  background: var(--primary-400) !important;
  border-color: var(--primary-400) !important;
  color: white !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

@media (max-width: 768px) {
  .login-container {
    max-width: 100%;
    padding: var(--spacing-4);
  }

  .login-form {
    padding: var(--spacing-6);
  }

  .title {
    font-size: var(--text-xl);
  }
}
</style>