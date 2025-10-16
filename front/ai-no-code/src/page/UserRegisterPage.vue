<template>
  <div id="userRegisterPage">
    <div class="register-container">
      <div class="register-form">
        <div class="form-header">
          <Icon icon="mdi:application-brackets" class="form-logo-icon" />
          <h2 class="title">AppCraft</h2>
        </div>
        <div class="desc">
          <Icon icon="mdi:account-plus-outline" class="desc-icon" />
          <span>创建账号，开启AI应用之旅</span>
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
          <a-form-item name="checkPassword" :rules="[
            { required: true, message: '请确认密码' },
            { min: 8, message: '密码不能小于 8 位' },
            { validator: validateCheckPassword },
          ]">
            <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码">
              <template #prefix>
                <Icon icon="mdi:lock-check-outline" class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>
          <div class="tips">
            <Icon icon="mdi:information-outline" class="tips-icon" />
            <span>已有账号？</span>
            <RouterLink to="/user/login">去登录</RouterLink>
          </div>
          <a-form-item>
            <a-button type="primary" html-type="submit" style="width: 100%">
              <Icon icon="mdi:account-plus" class="btn-icon" />
              <span>注册</span>
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { Icon } from '@iconify/vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  try {
    const res = await userRegister(values)
    // 注册成功，跳转到登录页面
    if (res.data.code === 0) {
      message.success('注册成功')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } catch (error) {
    message.error('注册失败，请稍后重试')
  }
}
</script>

<style scoped>
#userRegisterPage {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--secondary-600) 0%, var(--primary-600) 50%, var(--accent-600) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-4);
  position: relative;
}

#userRegisterPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 40%, rgba(27, 179, 111, 0.15), transparent 50%),
    radial-gradient(circle at 70% 60%, rgba(255, 220, 0, 0.15), transparent 50%);
  pointer-events: none;
}

.register-container {
  width: 100%;
  max-width: 420px;
  padding: var(--spacing-5);
  z-index: 2;
  position: relative;
}

.register-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 2px solid var(--primary-500);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-10);
  box-shadow: var(--shadow-2xl);
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

.register-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, var(--secondary-600), var(--primary-600), var(--accent-500));
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
  background: linear-gradient(135deg, var(--secondary-600), var(--primary-600));
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

.register-form :deep(.ant-btn-primary) .btn-icon {
  font-size: 18px;
  margin-right: var(--spacing-2);
}

.tips a {
  color: var(--secondary-600);
  text-decoration: none;
  font-weight: var(--font-medium);
  transition: var(--transition-colors);
}

.tips a:hover {
  color: var(--accent-500);
}

/* 覆盖ant-design表单样式 */
.register-form :deep(.ant-input),
.register-form :deep(.ant-input-password) {
  border: 2px solid var(--primary-200) !important;
  border-radius: var(--radius-lg) !important;
  padding: var(--spacing-3) var(--spacing-4) !important;
  font-size: var(--text-base) !important;
  transition: var(--transition-all) !important;
}

.register-form :deep(.ant-input:focus),
.register-form :deep(.ant-input-password-focused) {
  border-color: var(--primary-600) !important;
  box-shadow: 0 0 0 3px rgba(27, 179, 111, 0.2) !important;
}

.register-form :deep(.ant-btn-primary) {
  background: var(--primary-600) !important;
  border-color: var(--primary-600) !important;
  color: white !important;
  font-weight: var(--font-semibold) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-md) !important;
  transition: var(--transition-all) !important;
  height: 48px !important;
  font-size: var(--text-base) !important;
}

.register-form :deep(.ant-btn-primary:hover) {
  background: var(--accent-500) !important;
  border-color: var(--accent-500) !important;
  color: var(--gray-900) !important;
  transform: translateY(-2px) !important;
  box-shadow: var(--shadow-lg) !important;
}

@media (max-width: 768px) {
  .register-container {
    max-width: 100%;
    padding: var(--spacing-4);
  }

  .register-form {
    padding: var(--spacing-6);
  }

  .title {
    font-size: var(--text-xl);
  }
}
</style>