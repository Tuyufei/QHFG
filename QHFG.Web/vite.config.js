import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import cesium from "vite-plugin-cesium";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

// https://vite.dev/config/
export default defineConfig({
  css: {
    preprocessorOptions: {
      scss: {
        api: "modern-compiler",
      },
    },
  },
  configureWebpack:{
        externals:{
            'vue':'Vue',
            'vue-router':'VueRouter',
            'vuex':'Vuex',
            'element-ui':'ELEMENT',
            'axios':'axios'
        }
    },
  plugins: [
    vue(),
    vueDevTools(),
    cesium(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  optimizeDeps: {
    exclude: ["js-big-decimal"],
  },
  server: {
    proxy: {
      '^/map/get': {    //^ 符号表示只匹配以 /map/get 开头的路径
        target: 'http://129.211.174.149:18080',
        changeOrigin: true,
      },
      '/statistical': {
        target: 'http://129.211.174.149:18080',
        changeOrigin: true,
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
        output:{
            manualChunks(id) {
              if (id.includes('node_modules')) {
                  return id.toString().split('node_modules/')[1].split('/')[0].toString();
              }
          }
        }
    }

  },
});
