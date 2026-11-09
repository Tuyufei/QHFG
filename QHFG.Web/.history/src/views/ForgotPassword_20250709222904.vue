<template>
    <div class="forgot-pwd-page">
      <div class="forgot-pwd-box">
        <h2>重置密码</h2>
        <el-form :model="form" :rules="rules" ref="resetForm" label-width="100px">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入注册邮箱" />
            <el-button
              style="margin-top: 10px"
              :disabled="countdown > 0"
              @click="sendCode"
            >
              {{ countdown > 0 ? countdown + 's后重试' : '获取验证码' }}
            </el-button>
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <el-input v-model="form.code" placeholder="请输入验证码" />
          </el-form-item>
          <el-form-item label="新密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPwd">
            <el-input v-model="form.confirmPwd" type="password" placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submit" style="width:100%">重置密码</el-button>
          </el-form-item>
          <el-form-item>
            <el-link @click="$router.push('/home/login')">返回登录</el-link>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive } from 'vue';
  import { useRouter } from 'vue-router';
  import { ElMessage } from 'element-plus';
  import axios from 'axios';
  import { sendResetPwdCode, resetPassword } from '@/request';

  
  const router = useRouter();
  const countdown = ref(0);
  let timer = null;
  
  const form = reactive({
    email: '',
    code: '',
    password: '',
    confirmPwd: ''
  });
  
  const rules = {
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
    ],
    code: [
      { required: true, message: '请输入验证码', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码至少6位', trigger: 'blur' }
    ],
    confirmPwd: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== form.password) {
            callback(new Error('两次输入的密码不一致'));
          } else {
            callback();
          }
        },
        trigger: 'blur'
      }
    ]
  };
  
  const resetForm = ref(null);
  
  // 发送验证码
  const sendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请输入邮箱');
    return;
  }
  try {
    const params = {
      email: form.email,
    };
    const res = await sendResetPwdCode(params);
    if (res.code === 200) {
      ElMessage.success('验证码发送成功');
      countdown.value = 60;
      timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
          timer = null;
        }
      }, 1000);
    } else if (res.code === 415) {
      ElMessage.error('邮箱格式不正确');
    } else if (res.code === 412) {
      ElMessage.error('用户邮箱不存在');
    } else {
      ElMessage.error(res.msg || '发送失败');
    }
  } catch (e) {
    ElMessage.error('网络错误');
  }
};
  
  // 提交重置密码
  const submit = () => {
    resetForm.value.validate(async (valid) => {
      if (!valid) return;
      try {
        const res = await resetPassword({
          email: form.email,
          code: form.code,
          password: form.password // 这里必须是 password
        });
        if (res.code === 200) {
          ElMessage.success('修改成功，请重新登录');
          router.push('/home/login');
        } else if (res.code === 411) {
          ElMessage.error('验证码错误');
        } else {
          ElMessage.error(res.msg || '修改失败');
        }
      } catch (e) {
        ElMessage.error('网络错误');
      }
    });
  };

  </script>
  
  <style scoped>
  .forgot-pwd-page {
    min-height: 100vh;
    background: url('@/assets/img/register.png') center/cover no-repeat;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .forgot-pwd-box {
    width: 400px;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 0 20px rgba(0,0,0,0.08);
    padding: 32px 24px 12px 24px;
  }
  </style>
  