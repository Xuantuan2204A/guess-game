import axios from 'axios';

const api = axios.create({
  baseURL: '/api', // Dùng path tương đối → nginx sẽ proxy sang backend
  timeout: 10000,
  withCredentials: true
});

// Request Interceptor
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

// Response Interceptor (Xử lý Auto-Refresh)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Nếu lỗi 401 (Hết hạn Access Token) và chưa từng retry lần nào
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // Đánh dấu là đang thử lại để tránh lặp vô hạn
      
      try {
        // Âm thầm gọi API refresh (Cookie sẽ tự động được gửi ngầm nhờ withCredentials)
        const res = await api.post('/auth/refresh');
        
        // Lấy token mới lưu vào LocalStorage
        const newAccessToken = res.data.data.token;
        localStorage.setItem('token', newAccessToken);
        
        // Gắn token mới vào cái request vừa bị lỗi và ĐÁNH LẠI request đó
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
        return api(originalRequest);
        
      } catch (refreshError) {
        // Nếu refresh cũng thất bại (Cookie hết hạn luôn), đá văng ra màn hình Login
        console.warn("Phiên đăng nhập hết hạn hoàn toàn!");
        localStorage.removeItem('token');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;