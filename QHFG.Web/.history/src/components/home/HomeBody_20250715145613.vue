<template>
  <div 
    class="full-page-container" 
    @wheel="handleWheel"
    :style="{transform: `translateY(-${currentPage * 100}vh)`}"
    >

    <section 
      class="page" 
      :style="{  zIndex: currentPage === 0 ? 10 : 1  }"
      >
      <div class="page-content page1">
        <h1> {{ $t("message.systemTitle") }}</h1>
      </div>
    </section>

    <section 
      class="page"
      :style="{  zIndex: currentPage === 1 ? 10 : 1 }"
      >
      <div class="page-content page2">
        <h1>研究主题</h1>
        <div class="news-cards">
          <div
            class="card"
            v-for="(item, index) in themeList"
            :key="item.themeID"
            :class="{ 'card-highlight': themeIndex === index }"
             
             @click="selectTheme(item, index)"
          >
          
          <!-- 独立图片标签 -->
          <img :src="item.backgroundImage" alt="theme image" class="card-img">
          <!-- 标题放在图片下方 -->
          <h3 class="card-title">{{ $t(item.label) }}</h3>
        </div>
        </div>
      </div>
    </section>

   <section 
    class="page"
    :style="{ zIndex: currentPage === 2 ? 10 : 1 }"
    >
    <div class="page-content page3">
      <h2>研发团队</h2>
      <div class="teams">
        <div class="team">
           <div class="info_item">
          <div class="title">
            <span></span>
            <span class="label">{{ $t("message.Home.friendshipLink") }}</span>
          </div>
          <a href="https://www.tsinghua.edu.cn/xxgk.htm">{{ $t("message.Home.friendshipLink1") }}</a>
          <a href="https://www.env.tsinghua.edu.cn/">{{ $t("message.Home.friendshipLink2") }}</a>
          <a href="https://www.icon.tsinghua.edu.cn/">{{ $t("message.Home.friendshipLink3") }}</a>
        </div>
        </div>
        <div class="team">
           <div class="info_item">
          <div class="title">
            <span></span>
            <span class="label">{{ $t("message.Home.contactUs") }}</span>
          </div>
          <span>{{ $t("message.Home.address") }}</span>
          <span>{{ $t("message.Home.post_code") }}</span>
          <span>{{ $t("message.Home.telephone") }}</span>
          <span>{{ $t("message.Home.fax") }}</span>
        </div>
        </div>
        <div class="team">
           <div class="follow_us">
          <div class="title">
            <span></span>
            <span class="label">{{ $t("message.Home.followUs") }}</span>
          </div>
          <div class="content">
            <div class="item">
              <img src="/static/img/环境学院公众号.png" alt="" />
              <span>{{ $t("message.Home.qr_code1") }}</span>
            </div>
            <div class="item">
              <img src="/static/img/碳中和研究院公众号.jpg" alt="" />
              <span>{{ $t("message.Home.qr_code2") }}</span>
            </div>
          </div>
        </div>
        </div>
      </div>
      <div class="bottom">
        <span class="copyright">Copyright ©2024 清华大学环境学院 版权所有</span>
      </div>
      
    </div>
   </section>

   

  </div>

  <!-- 右侧圆点导航 -->
    <div class="page-nav">
      <div 
        class="nav-dot" 
        v-for="index in 3" 
        :key="index"
        :class="{ active: currentPage === index-1 }"
        @click="currentPage = Math.min(Math.max(index-1, 0), 2)"
      ></div>
    </div>


 
</template>

<script setup>
import { ref, reactive } from "vue";
// import img1 from '@/assets/img/HomeBody1.jpg'; // 新增




import { useRouter } from "vue-router";



const router = useRouter();

// 替代选项式的data
const currentPage = ref(0); // 当前页面索引
const isScrolling = ref(false); // 防抖标记

// 替代选项式的methods
const handleWheel = (e) => {
  if (isScrolling.value) return;
  isScrolling.value = true;
  setTimeout(() => isScrolling.value = false, 500);

  e.preventDefault();
  const deltaY = e.deltaY;

  if (deltaY > 0) {
    currentPage.value = Math.min(currentPage.value + 1, 2);
  } else {
    currentPage.value = Math.max(currentPage.value - 1, 0);
  }

   console.log("当前页面索引：", currentPage.value); 
};


// 主题列表


const themeList = reactive([
  {
    label: "message.Home.theme1",
    path: "/home/nav-page1",
    themeID: "theme1",
    backgroundImage: img1 
  },
  { 
    label: "message.Home.theme2", 
    path: "/home/nav-page2",
    themeID: "theme2",
    backgroundImage: img1 
  },
  { 
    label: "message.Home.theme3", 
    path: "/home/nav-page3",
    themeID: "theme3",
    backgroundImage: img1 
  }
]);

const themeIndex = ref(0);

const selectTheme = (item, index) => {
  themeIndex.value = index;
  router.push({
    path: item.path,
  });
};



</script>

<style lang="scss" scoped>

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  overflow: hidden; // 关键：隐藏浏览器默认滚动条
  height: 100%; // 确保高度充满视口
  margin: 0; // 清除默认边距（避免意外溢出）
  padding: 0; // 清除默认内边距
}

/* 容器：全屏 + 隐藏滚动条 */
.full-page-container {
  height: 300vh;
  width: 100vw; // 占满屏幕宽度
  position: relative; // 作为.page的定位基准
  transition: transform 0.5s ease; // 页面切换动画
  overflow: hidden;
  background: #ff0000; /* 容器背景设为红色（调试用） */
  //border: 5px solid #000; /* 容器边框（调试用） */
  margin: 0;
  padding: 0;
}

/* 单个页面：全屏 + 绝对定位 + 过渡动画 */
.page {
  height: 100vh;
  width: 100vw;
  position: relative;
  //position:absolute;
  top: 0;
  left: 0;
  //border: 3px solid #fff; /* 页面边框（区分页面） */
}

/* 页面内容容器：垂直居中 */
.page-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  //justify-content: center;
  justify-content: flex-start;
  align-items: center;
  padding-top: 60px; // 顶部留适当间距（替代冗余的margin-top）
  padding-bottom: 60px; // 底部留间距，避免贴边
  max-height: 100vh; // 限制内容容器最大高度为视口高度
  
  box-sizing: border-box;

  
}


.page1 {
  position:relative;
  //background: url('@/assets/bg1.jpg') center/cover no-repeat;
  background-color: #2c3e50;
  color: white;
  //text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.5);
  text-align: center;
  justify-content: flex-start; /* 内容靠上 */
  padding-top: 100px; /* 配合靠上布局，增加顶部间距 */
 
  background-image: url('@/assets/img/home.png'); /* 图片路径 */
  background-size: cover; /* 图片拉伸/压缩至填满容器，保持比例 */
  background-position: center; /* 图片居中显示 */
  background-repeat: no-repeat; /* 禁止重复平铺 */
   /* 渐变蒙版：通过伪元素实现 */
  &::before {
    content: '';
    position: absolute; /* 相对于.page1定位 */
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    /* 渐变效果：可根据需求调整方向和透明度 */
    /* 示例：从上到下，从浅黑（透明）到深黑（半透明） */
    background: linear-gradient(
      to bottom,
      rgba(0, 0, 0, 0.2) 0%,  /* 顶部透明度低 */
      rgba(0, 0, 0, 0.5) 100% /* 底部透明度高 */
    );
    z-index: 1; /* 蒙版层级：在背景图之上，标题之下 */
  }
  h1{
    // font-size: 2.5rem;
    // line-height: 1.5;
    // max-width: 80%;
    // text-shadow: 0 2px 4px rgba(0,0,0,0.5); /* 文字添加阴影，增强与背景的对比度 */

    //  color: #050000;
    font-size: 3rem; /* 增大字体 */
    line-height: 1.6; /* 行高 */
    max-width: 80%; /* 限制最大宽度，避免过宽 */
    margin: 0 auto; /* 水平居中 */
    padding-top: 150px; /* 调整垂直位置（替代原padding-top） */
    color: #ffffff; /* 白色字体，与蒙版对比 */
    text-shadow: 0 3px 6px rgba(0, 0, 0, 0.8); /* 增强文字阴影，提升可读性 */
    z-index: 2; /* 标题层级：在蒙版之上 */
    position: relative; /* 确保z-index生效 */


  }

  

}
.page2 {
  background-color: #ffffff;
  h1{
    font-size: 2.5rem;
    margin: 0 0 30px 0; // 仅保留底部间距，删除顶部冗余margin
    text-align: center; // 确保标题居中
    //color: #007bff;
    color: #3498db;
    margin-top: auto; 
  }
  .news-cards{
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    justify-content: center;
    width: 100%; /* 确保容器占满页面宽度 */
    margin-top: 20px;
    max-height: calc(100vh - 200px); // 最大高度 = 视口高度 - 标题和上下padding的总高度
    overflow-y: auto; // 当卡片过多时，允许内部滚动（避免整体溢出）
    padding: 10px 0; 
  }
  .card{
    //width: 300px;
    flex: 1 1 calc(33.333% - 60px); /* 一行最多3个卡片（33.333%），减去左右gap的一半（30px*2） */
    min-width: 280px; /* 最小宽度，避免窄屏时卡片过窄 */
    max-width: 400px; /* 最大宽度，避免宽屏时卡片过大 */
    height: auto; /* 高度自动，由内容撑开（图片200px + 标题高度） */
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    overflow: hidden;
    //background: white;
    background-color: transparent; /* 透明背景，避免覆盖图片 */
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    &:hover {
      transform: translateY(-20px); /* 轻微上浮 */
      box-shadow: 0 8px 16px rgba(0,0,0,0.15); /* 加深阴影 */
    }

     /* 新增：背景图设置 */
    background-size: cover;       // 覆盖整个卡片
    background-position: center;  // 居中显示
    background-repeat: no-repeat; // 不重复
    .card-img{
      width: 100%;
      height: auto;
      object-fit: cover;
      border-radius: 8px 8px 0 0;
      margin-bottom: 15px; /* 增加图片与标题的间距（数值可调整） */
    }
    .card-title{
      padding: 10px;
      background-color: #56b4fc;
      width: 100%;
      margin: 0;
      font-size: 1.2rem;
      color: #000;
      
    }

  }
}
.page3 {
  background-color: #5db1e9;
  .page-content{
    justify-content: flex-start;
    padding: 60px 40px;
    max-height: 100vh;
    width: 100%;
  }
  h2{
    font-size: 2.2rem;
    margin: 20px 0 50px 0;
    text-align: center;
    text-shadow: 0 1px 3px rgba(0,0,0,0.2);
    color: #f6f8fa;
  }
  .teams{
    display: flex;
    gap: 30px;
    flex-wrap:wrap ;
    justify-content: center;
    width: 100%;
    max-width: 1200px;
    margin-bottom: 60px;
  }
  .team{
    flex:1 1 calc(33.333% - 40px);
    min-width:280px;
    max-width:350px;

    background: rgb(235, 245, 246);
    border-radius: 10px;
    padding: 30px 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.2rem;
    color: #2c3e50;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  }
  .title {
    display: flex;
    align-items: center;
    margin-bottom: 25px;
    color: #3498db;

    span:first-child {
      width: 4px;
      height: 20px;
      background-color: #3498db;
      margin-right: 10px;
    }

    .label {
      font-size: 1.3rem;
      font-weight: bold;
    }
  }

  .info_item {
    display: flex;
    flex-direction: column;
    gap: 15px;

    a, span {
      color: #555;
      font-size: 1rem;
      text-decoration: none;

      &:hover {
        color: #3498db;
      }
    }
  }

  .follow_us .content {
    display: flex;
    flex-direction: column;
    gap: 20px;
    align-items: center;
  }

  .follow_us .item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }

  .follow_us img {
    width: 140px;
    height: 140px;
    object-fit: cover;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }

  .follow_us span {
    color: #555;
    font-size: 0.95rem;
    text-align: center;
  }

  .bottom {
    margin-top: 50px;
    width: 100%;
    text-align: center;
  }

  .copyright {
    color: #fff;
    font-size: 0.9rem;
    opacity: 0.9;
  }
}
/* 右侧导航点样式（关键：放在container外面，避免被overflow隐藏） */
.page-nav {
  position:fixed; /* 固定在右侧，不随页面滚动 */
  right: 30px;
  top: 50%;
  transform: translateY(-50%); /* 垂直居中 */
  display: flex;
  flex-direction: column;
  gap: 15px; /* 圆点之间的间距 */
  z-index: 100; /* 确保在所有页面上方显示 */
}

.nav-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%; /* 圆形 */
  background-color: rgba(9, 169, 233, 0.5); /* 未激活状态：半透明白色 */
  cursor: pointer;
  transition: all 0.3s ease; /* 点击/切换时的动画 */
  border: 2px solid transparent;
}

/* 激活状态的圆点（当前页面） */
.nav-dot.active {
  background-color: white; /* 白色填充 */
  transform: scale(1.3); /* 轻微放大 */
  border-color: #3498db; /* 蓝色边框高亮 */
}

</style>
