import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import GameView from '../views/GameView.vue';
import PaymentResultView from '../views/PaymentResultView.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginView },
  { path: '/register', component: RegisterView },
  { path: '/game', component: GameView },
  { path: '/payment-result', component: PaymentResultView }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;