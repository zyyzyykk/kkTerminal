import { createRouter, createWebHistory } from 'vue-router';

import TerminalView from "@/views/TerminalView";
import AccessCheck from "@/views/AccessCheck";

const routes = [
  {
    path: '/terminal',
    name: 'terminal',
    component: TerminalView,
  },
  {
    path: '/login',
    name: 'login',
    component: AccessCheck,
  },
  {
    path: '/',
    redirect: '/terminal',
  },
  {
    path: '/:catchAll(.*)',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
