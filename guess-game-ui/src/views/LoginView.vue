<template>
  <div class="login-wrapper">
    <div class="login-box">
      <h2>Đăng Nhập Guess Game</h2>
      
      <!-- Bắt sự kiện submit form, prevent để không bị reload lại trang -->
      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <label>Tên đăng nhập</label>
          <input v-model="username" type="text" required placeholder="Nhập username..." />
        </div>
        
        <div class="input-group">
          <label>Mật khẩu</label>
          <input v-model="password" type="password" required placeholder="Nhập mật khẩu..." />
        </div>
        
        <button type="submit">Vào Chơi Ngay</button>
      </form>
      
      <!-- Hiển thị lỗi nếu sai mật khẩu -->
      <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>
      <div class="switch-page" style="margin-top: 20px; font-size: 14px;">
        <span>Chưa có tài khoản? </span>
        <router-link to="/register" style="color: #10b981; text-decoration: none; font-weight: bold;">Tạo ngay</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '../utils/api'; // Import cái api.js vừa tạo

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const router = useRouter();

const handleLogin = async () => {
  errorMessage.value = ''; // Reset lỗi cũ
  try {
    // Gọi API sang Spring Boot
    const response = await api.post('/auth/login', {
      username: username.value,
      password: password.value
    });
    
    // Lấy token từ API trả về và lưu vào trình duyệt
    const token = response.data.data.token;
    localStorage.setItem('token', token);
    
    // Đăng nhập thành công -> Chuyển hướng sang trang Game
    router.push('/game');
    
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng thử lại!';
  }
};
</script>

<style scoped>
/* Code CSS cơ bản cho form dễ nhìn */
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
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
button:hover {
  background-color: #059669;
}
.error-msg {
  color: #ef4444;
  margin-top: 15px;
  font-size: 14px;
}
</style>