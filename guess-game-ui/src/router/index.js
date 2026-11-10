import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import GameView from '../views/GameView.vue';
import PaymentResultView from '../views/PaymentResultView.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/register', component: RegisterView, meta: { public: true } },
  { path: '/game', component: GameView, meta: { requiresAuth: true } },
  { path: '/payment-result', component: PaymentResultView, meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Navigation Guard: chặn truy cập nếu chưa đăng nhập
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const isLoggedIn = !!token;

  if (to.meta.requiresAuth && !isLoggedIn) {
    // Chưa đăng nhập mà vào trang cần auth → đá về login
    next('/login');
  } else if (to.meta.public && isLoggedIn) {
    // Đã đăng nhập mà vào login/register → redirect thẳng vào game
    next('/game');
  } else {
    next();
  }
});

export default router;