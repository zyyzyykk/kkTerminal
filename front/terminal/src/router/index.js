import { createRouter, createWebHistory } from "vue-router";

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
  history: createWebHistory((window.location.pathname + '/')
      .replace(/\/{2,}/g, '/')
      .replace(/\/index\.html\/$/, "/")),
  routes,
});

export default router;
