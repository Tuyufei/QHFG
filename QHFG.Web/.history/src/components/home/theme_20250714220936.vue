<template>
  <div class="layout">
    <!--<aside class="sidebar">
      <div class="return-button">
        <el-button class="button" @click="returnButton()" type="primary">{{ $t(`message.return`) }}</el-button>
      </div>
      <ul class="menu-list">
        <li v-for="item in themes" :key="item.id">
          <router-link :to="item.path" class="menu-item" active-class="active">
            <h3>{{ item.name || $t(`${themeName}.paper${item.id}.title`) }}</h3>
            <p class="time">{{ item.time }}</p>
          </router-link>
        </li>
      </ul>
    </aside>-->
    <main class="content">
      <div class="themeTitle">{{ $t('message.Home.' + themeName) }}</div>
      <hr>
      <router-view />
      <div class="nav-btns">
        <button @click="goPrev" :disabled="!hasPrev">{{$t("theme.go_back")}}</button>
        <button @click="goNext" :disabled="!hasNext">{{$t("theme.go_next")}}</button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue';
import {useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n';
const { locale } = useI18n();

// 监听语言变化并更新列表
watch(locale, () => {
  
});

const props = defineProps({
  themeName: {
    type: String,
    default: 'theme1'
  },
  themes: {
    type: Array as () => Array<{ id: number, time: string, path: string, name?: string }>,
    default: () => []
  },
  themeTitle: {
    type: String,
    default: ''
  }
})

const router = useRouter();
const route = useRoute();
const returnButton = () => {
  router.push('/home')
}
const currentIndex = computed(() => {
  return props.themes.findIndex(item => item.path === route.path);
});

const hasPrev = computed(() => currentIndex.value > 0);
const hasNext = computed(() => currentIndex.value < props.themes.length - 1);

function goPrev() {
  if (hasPrev.value) {
    router.push(props.themes[currentIndex.value - 1].path);
  }
}
function goNext() {
  if (hasNext.value) {
    router.push(props.themes[currentIndex.value + 1].path);
  }
}
onMounted(() => {
  if (currentIndex.value === -1) {
    router.push(props.themes[0].path);
  }
})

</script>

<style lang="scss" scoped>
a {
  color: #333; /* 调整字体颜色 */
  font-size: 24px; /* 增大字体 */
  text-align: center; /* 文本居中 */
}
hr {
  width: 800px;
  margin: 20px auto;
  background: #007bff;
}
.themeTitle {
  font-size: 40px;
  text-align: center;
  line-height: 40px;
  font-weight: 700;
  color: #007bff;
  padding: 0 20px;
}
.layout {
  display: flex;
  min-height: 100vh;
  /*.sidebar {
    position: fixed; // 固定定位
    // top: 10; // 距离窗口顶部为0
    // left: 0; // 距离窗口左侧为0
    width: 300px; // 固定宽度
    height: 100vh; // 高度占满整个视口
    z-index: 100; // 保证在内容区之上
    background: #f8f9fa; // 建议加背景色
    box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
    padding: 20px 10px 0 10px; // 顶部内边距
    display: flex;
    flex-direction: column;

    .return-button {
      color: #007bff;
      margin-bottom: 20px;
      padding: 0 10px;
      .button {
        width: 100px;
        height: 40px
      }
    }
*/
    // .menu-list {
    //   list-style: none;
    //   padding: 0 10px;
    //   margin: 0;
    //   overflow-y: auto;
    //   flex: 1;

    //   .menu-item {
    //     display: block;
    //     margin-bottom: 15px;
    //     padding: 10px 15px;
    //     border-radius: 8px;
    //     text-decoration: none;
    //     color: #333;
    //     transition: all 0.3s ease;
    //     background-color: #ffffff;
    //     box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);

    //     h3 {
    //       margin: 0;
    //       font-size: 16px;
    //       color: #007bff;
    //       word-wrap: break-word;
    //       text-align: left;
    //     }

    //     .time {
    //       font-size: 14px;
    //       color: #666;
    //       margin-top: 5px;
    //       text-align: left;
    //     }

    //     &:hover {
    //       background-color: #e9f5ff;
    //       color: #0056b3;
    //       transform: translateY(-2px);
    //       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    //     }

    //     &.active {
    //       background-color: #007bff;
    //       color: #ffffff;
    //       transform: translateY(-2px);
    //       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);

    //       h3 {
    //         color: #ffffff;
    //       }

    //       .time {
    //         color: #ffffff;
    //       }
    //     }
    //   }
    // }
  }
  .content {
    flex: 1;
    margin-left: 300px;
    padding: 30px; /* 增加内边距 */
    background-color: #f8f9fa; /* 添加一个浅色背景 */
    justify-content: center; /* 水平居中内容 */
    align-items: center; /* 垂直居中内容 */
    font-size: 24px; /* 增大字体 */
    color: #333; /* 调整字体颜色 */
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05); /* 添加内部阴影 */
    .title,
    .subtitle {
      text-align: center; /* 文本居中 */
    }
    .subtitle {
      font-size: 18px; /* 调整副标题字体大小 */
      margin-bottom: 20px; /* 增加副标题与描述之间的间距 */
    }
    .image {
      // margin: 0 auto;
      width: 100%;
      height: auto;
    }
    .nav-btns {
      display: flex;
      justify-content: center;
      gap: 20px;
    }
    button {
      margin: 0px 0px 40px 0px;
      padding: 8px 24px;
      border-radius: 6px;
      border: none;
      background: #007bff;
      color: #fff;
      font-size: 16px;
      cursor: pointer;
      transition: 0.2s;
    }
    button:disabled {
      background: #ccc;
      cursor: not-allowed;
    }
  }
//}
</style>
