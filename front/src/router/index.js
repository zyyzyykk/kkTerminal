import { createRouter, createWebHistory } from "vue-router";
import { router_base_path } from "@/env/Base";

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
  history: createWebHistory(router_base_path),
  routes,
});

export default router;
