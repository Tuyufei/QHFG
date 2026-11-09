<!-- <template>
    <div class="nav-btns">
      <button @click="goPrev" :disabled="!hasPrev">上一篇</button>
      <button @click="goNext" :disabled="!hasNext">下一篇</button>
    </div>
  </template> -->
  <template>
    <div class="nav-btns">
      <div class="nav-info" v-if="showTitle">
        <span class="current-title">当前：{{ currentArticle?.title }}</span>
      </div>
      <div class="nav-buttons">
        <button 
          @click="goPrev" 
          :disabled="!hasPrev" 
          :title="prevArticle?.title"
        >
          <span class="arrow">←</span> 上一篇
        </button>
        <button 
          @click="goNext" 
          :disabled="!hasNext"
          :title="nextArticle?.title"
        >
          下一篇 <span class="arrow">→</span>
        </button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { useRouter, useRoute } from 'vue-router';
  import { computed } from 'vue';
  
  // 文章列表（可从父组件props传递，这里直接写死示例）
  const themes = [
    { id: 1, path: '/home/theme1/paper1', title: '文章1标题' },
    { id: 2, path: '/home/theme1/paper2', title: '文章2标题' },
    { id: 3, path: '/home/theme1/paper3', title: '文章3标题' },
    { id: 4, path: '/home/theme1/paper4', title: '文章4标题' },
  ];
  
  const router = useRouter();
  const route = useRoute();
  
  const currentIndex = computed(() => {
    return themes.findIndex(item => item.path === route.path);
  });
  
  const hasPrev = computed(() => currentIndex.value > 0);
  const hasNext = computed(() => currentIndex.value < themes.length - 1);
  
  function goPrev() {
    if (hasPrev.value) {
      router.push(themes[currentIndex.value - 1].path);
    }
  }
  function goNext() {
    if (hasNext.value) {
      router.push(themes[currentIndex.value + 1].path);
    }
  }
  </script>
  
  <style lang="scss" scoped>
  /* .nav-btns {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin: 40px 0 0 0;
  } */
  .nav-btns {
  position: sticky;
  bottom: 0;
  left: 0;
  width: 100%;
  background: linear-gradient(to top, rgba(255, 255, 255, 1) 60%, rgba(255, 255, 255, 0.9) 100%);
  padding: 20px 0;
  margin: 0;
  z-index: 10;
  
  .nav-info {
    text-align: center;
    margin-bottom: 15px;
    .current-title {
      font-size: 16px;
      color: #666;
    }
  }

  button {
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
</style>