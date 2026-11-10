<template>
  <div class="game-container">
    <!-- Header -->
    <header class="header">
      <h2>Chào mừng, {{ userInfo.username }}!</h2>
      <button @click="logout" class="btn-logout">Đăng xuất</button>
    </header>

    <div class="main-content">
      <!-- Cột Trái: Khu vực chơi game -->
      <div class="play-area">
        <div class="stats-card">
          <h3>Thông tin của bạn</h3>
          <p>Điểm số (Score): <strong>{{ userInfo.score }}</strong></p>
          <p>Lượt chơi (Turns): <strong>{{ userInfo.turns }}</strong></p>
          <button @click="buyTurns" class="btn-buy" :disabled="isBuying">
            {{ isBuying ? 'Đang tạo link...' : 'Mua thêm 5 lượt (VNPAY)' }}
          </button>
        </div>

        <div class="guess-card">
          <h3>Dự đoán con số (1 - 5)</h3>
          <form @submit.prevent>
            <input 
              v-model="guessNumber" 
              type="number" 
              min="1" max="5" 
              required 
              placeholder="Nhập số từ 1 đến 5..."
              @keyup.enter="playGame"
            />
            <button type="button" @click="playGame" class="btn-play" :disabled="userInfo.turns <= 0 || isPlaying">
              {{ isPlaying ? 'Đang quay...' : 'ĐOÁN NGAY' }}
            </button>
          </form>
          
          <!-- Thông báo kết quả trúng/trượt -->
          <div v-if="gameMessage" :class="['result-msg', isWin ? 'text-green' : 'text-red']">
            {{ gameMessage }}
          </div>
        </div>
      </div>

      <!-- Cột Phải: Bảng xếp hạng (Redis) -->
      <div class="leaderboard-area">
        <div class="leaderboard-card">
          <h3>🏆 Bảng Xếp Hạng (Top 10)</h3>
          <ul>
            <li v-for="(user, index) in leaderboard" :key="index">
              <span class="rank">#{{ index + 1 }}</span>
              <span class="name">{{ user.username }}</span>
              <span class="score">{{ user.score }} pts</span>
            </li>
            <li v-if="leaderboard.length === 0" style="text-align: center; color: gray;">
              Chưa có ai ghi điểm.
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../utils/api';

const router = useRouter();
const userInfo = ref({ username: '', score: 0, turns: 0 });
const leaderboard = ref([]);
const guessNumber = ref('');
const gameMessage = ref('');
const isWin = ref(false);
const isPlaying = ref(false);
const isBuying = ref(false);

// Hàm tải dữ liệu ban đầu
const fetchData = async () => {
  try {
    const [meRes, leadRes] = await Promise.all([
      api.get('/me'),
      api.get('/leaderboard')
    ]);
    userInfo.value = meRes.data.data;
    leaderboard.value = leadRes.data.data;
  } catch (error) {
    console.error("Lỗi lấy dữ liệu:", error);
  }
};

// Hàm xử lý chơi game
const playGame = async () => {
  if (userInfo.value.turns <= 0) {
    alert("Bạn đã hết lượt chơi!");
    return;
  }
  
  isPlaying.value = true;
  gameMessage.value = '';
  
  try {
    const response = await api.post('/game/guess', { guessNumber: guessNumber.value });
    const result = response.data.data;
    
    isWin.value = result.isWin;
    gameMessage.value = response.data.message;
    
    // Cập nhật điểm và lượt chơi ngay lập tức
    userInfo.value.score = result.currentScore;
    userInfo.value.turns = result.remainingTurns;
    
    // Refresh lại bảng xếp hạng (vì điểm có thể thay đổi)
    fetchData();
  } catch (error) {
    if (error.response?.status === 409) {
      gameMessage.value = "Phát hiện thao tác quá nhanh! Vui lòng không spam.";
      isWin.value = false;
    } else {
      gameMessage.value = error.response?.data?.message || "Có lỗi xảy ra!";
      isWin.value = false;
    }
  } finally {
    isPlaying.value = false;
    guessNumber.value = ''; // Xóa trắng ô input
  }
};

// Hàm gọi VNPAY
const buyTurns = async () => {
  isBuying.value = true;
  try {
    const response = await api.post('/payment/buy-turns');
    const paymentUrl = response.data.data;
    // Chuyển hướng trình duyệt sang cổng VNPAY
    window.location.href = paymentUrl;
  } catch (error) {
    alert("Lỗi khi tạo giao dịch thanh toán!");
    isBuying.value = false;
  }
};

// Đăng xuất
const logout = () => {
  localStorage.removeItem('token');
  router.push('/login');
};

// Chạy ngay khi mở trang
onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.game-container { max-width: 1000px; margin: 0 auto; padding: 20px; font-family: sans-serif; }
.header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #eee; padding-bottom: 15px; margin-bottom: 20px; }
.btn-logout { background-color: #ef4444; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }
.main-content { display: flex; gap: 20px; }
.play-area { flex: 2; display: flex; flex-direction: column; gap: 20px; }
.leaderboard-area { flex: 1; }
.stats-card, .guess-card, .leaderboard-card { background: #f9fafb; padding: 20px; border-radius: 8px; border: 1px solid #e5e7eb; }
h3 { margin-top: 0; color: #111827; }
.btn-buy { background-color: #f59e0b; color: white; border: none; padding: 10px; border-radius: 4px; cursor: pointer; font-weight: bold; width: 100%; margin-top: 10px; }
.btn-play { background-color: #3b82f6; color: white; border: none; padding: 10px; border-radius: 4px; cursor: pointer; font-weight: bold; width: 100%; margin-top: 10px; }
.btn-play:disabled, .btn-buy:disabled { background-color: #9ca3af; cursor: not-allowed; }
input { width: 100%; padding: 10px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
.result-msg { margin-top: 15px; padding: 10px; text-align: center; font-weight: bold; border-radius: 4px; background: #fff; border: 1px solid #eee; }
.text-green { color: #10b981; }
.text-red { color: #ef4444; }
.leaderboard-card ul { list-style: none; padding: 0; margin: 0; }
.leaderboard-card li { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
.leaderboard-card li:last-child { border-bottom: none; }
.rank { font-weight: bold; color: #f59e0b; width: 30px;}
.name { flex: 1; text-align: left; }
.score { font-weight: bold; color: #3b82f6; }
</style>