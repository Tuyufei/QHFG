import "./assets/base.scss";
import "./assets/icon/iconfont.css";
import "element-plus/theme-chalk/index.css";
import ElementPlus from "element-plus";
import language from "element-plus/dist/locale/zh-cn.mjs"; //如果是vite构建，引入部分会爆红，可以在vite-env.d.ts中declare一下
import Antd from "ant-design-vue";
import "ant-design-vue/dist/reset.css";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router/index.js";
// pinia持久化
import { createPinia } from "pinia";
import { createI18n } from "vue-i18n";
// import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
const pinia = createPinia();
// pinia.use(piniaPluginPersistedstate);
// 引入v-chart
import VChart, { THEME_KEY } from "vue-echarts";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { PieChart, LineChart, BarChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  //   DatasetComponent,
  GridComponent,
  PolarComponent,
} from "echarts/components";
use([
  CanvasRenderer,
  PieChart,
  BarChart,
  //   DatasetComponent,
  GridComponent,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  PolarComponent,
]);

// 导入语言包
import english from "@/assets/json/locales/en.json";
import chinese from "@/assets/json/locales/zh.json";

// 创建i18n实例
export const i18n = createI18n({
  legacy: false, // 禁用旧版API模式
  globalInjection: true, // 启用全局注入
  locale: "chinese", // 设置默认语言
  fallbackLocale: "english", // 设置备用语言
  messages: {
    english,
    chinese,
  },
  // silentTranslationWarn: true,
  // missingWarn: false,
  // silentFallbackWarn: false,
  // fallbackWarn: false,
});
const app = createApp(App);
app.use(pinia);
app.use(router);
app.use(ElementPlus, { language });
app.use(Antd);
app.use(i18n);
// 全局注册组件（也可以使用局部注册）
app.component("v-chart", VChart);
// 设置深色主题
// app.provide(THEME_KEY, "dark");

app.mount("#app");
export default app;
