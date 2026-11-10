<template>
  <div class="login-wrapper">
    <div class="login-box">
      <h2>Đăng Ký Tài Khoản</h2>
      
      <form @submit.prevent="handleRegister">
        <div class="input-group">
          <label>Tên đăng nhập</label>
          <input v-model="username" type="text" required placeholder="Tạo username..." />
        </div>
        
        <div class="input-group">
          <label>Mật khẩu</label>
          <input v-model="password" type="password" required placeholder="Tạo mật khẩu..." />
        </div>

        <div class="input-group">
          <label>Xác nhận mật khẩu</label>
          <input v-model="confirmPassword" type="password" required placeholder="Nhập lại mật khẩu..." />
        </div>
        
        <button type="submit">Đăng Ký</button>
      </form>
      
      <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>
      <p v-if="successMessage" class="success-msg">{{ successMessage }}</p>

      <div class="switch-page">
        <span>Đã có tài khoản? </span>
        <router-link to="/login">Đăng nhập</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../utils/api';

const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const router = useRouter();

const handleRegister = async () => {
  errorMessage.value = '';
  successMessage.value = '';

  if (password.value !== confirmPassword.value) {
    errorMessage.value = 'Mật khẩu xác nhận không khớp!';
    return;
  }

  try {
    const response = await api.post('/auth/register', {
      username: username.value,
      password: password.value
    });
    
    successMessage.value = 'Đăng ký thành công! Đang chuyển về Đăng nhập...';
    
    // Đợi 1.5s để user kịp đọc thông báo thành công rồi mới đá về trang login
    setTimeout(() => {
      router.push('/login');
    }, 1500);
    
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Tên đăng nhập đã tồn tại hoặc có lỗi!';
  }
};
</script>

<style scoped>
/* Dùng chung style với trang Login cho đồng bộ */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f3f4f6;
}
.login-box {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 350px;
  text-align: center;
}
.input-group {
  margin-bottom: 15px;
  text-align: left;
}
input {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #3b82f6; /* Nút đăng ký màu xanh dương cho khác biệt */
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
button:hover {
  background-color: #2563eb;
}
.error-msg {
  color: #ef4444;
  margin-top: 15px;
  font-size: 14px;
}
.success-msg {
  color: #10b981;
  margin-top: 15px;
  font-size: 14px;
}
.switch-page {
  margin-top: 20px;
  font-size: 14px;
}
.switch-page a {
  color: #3b82f6;
  text-decoration: none;
  font-weight: bold;
}
</style>