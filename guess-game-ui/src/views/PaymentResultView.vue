<template>
  <div class="result-container">
    <div class="result-box">
      <h2 v-if="isSuccess" style="color: #10b981;">🎉 Thanh Toán Thành Công!</h2>
      <h2 v-else style="color: #ef4444;">❌ Thanh Toán Thất Bại!</h2>
      
      <p v-if="isSuccess">
        {{ isUpdating ? 'Đang cập nhật 5 lượt chơi vào hệ thống...' : 'Đã cộng 5 lượt chơi thành công!' }}
        </p>
      <p v-else>Giao dịch của bạn đã bị hủy hoặc có lỗi xảy ra.</p>
      
      <button @click="backToGame">Quay lại trang Game</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '../utils/api'; // Import api để gọi xuống Spring Boot

const route = useRoute();
const router = useRouter();
const isSuccess = ref(false);
const isUpdating = ref(false); // Thêm trạng thái đang cập nhật DB

onMounted(async () => {
  // Đọc các tham số trên thanh URL do VNPAY trả về
  const responseCode = route.query.vnp_ResponseCode;
  const txnRef = route.query.vnp_TxnRef;
  
  if (responseCode === '00') {
    isSuccess.value = true;
    isUpdating.value = true;
    
    // MẸO LOCALHOST: Tự gọi API IPN để kích hoạt hàm cộng điểm ở Backend
    try {
      await api.get(`/payment/vnpay-ipn?vnp_TxnRef=${txnRef}&vnp_ResponseCode=${responseCode}`);
      console.log("Đã cập nhật điểm trong Database thành công!");
    } catch (error) {
      console.error("Lỗi khi cập nhật điểm:", error);
    } finally {
      isUpdating.value = false;
    }
    
  } else {
    isSuccess.value = false;
  }
});

const backToGame = () => {
  router.push('/game');
};
</script>