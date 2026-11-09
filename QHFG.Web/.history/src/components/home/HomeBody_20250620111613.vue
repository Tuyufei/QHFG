<template>
  <div class="home">
    <main>
      <div class="home_img">
        <div class="social_theme">
          <div
            class="theme"
            :class="themeIndex == index ? 'themeHighlight' : ''"
            v-for="(item, index) in themeList"
            :key="index"
            @click="selectTheme(item, index)"
          >
            {{ $t(item.label) }}
          </div>
        </div>
      </div>
    </main>
    <footer>
      <div class="info">
        <div class="info_item">
          <div class="title">
            <span></span>
            <span class="label">{{ $t("message.Home.friendshipLink") }}</span>
          </div>
          <a href="https://www.tsinghua.edu.cn/xxgk.htm">{{ $t("message.Home.friendshipLink1") }}</a>
          <a href="https://www.env.tsinghua.edu.cn/">{{ $t("message.Home.friendshipLink2") }}</a>
          <a href="https://www.icon.tsinghua.edu.cn/">{{ $t("message.Home.friendshipLink3") }}</a>
        </div>
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
      <div class="bottom">
        <span class="copyright">Copyright ©2024 清华大学环境学院 版权所有</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// 主题列表
const themeList = reactive([
  {
    label: "message.Home.theme1",
    path: "/home/theme1",
  },
  { label: "message.Home.theme2", path: "/home/theme2" },
  { label: "message.Home.theme3", path: "/home/theme3" },
]);

const themeIndex = ref(0);

const selectTheme = (item, index) => {
  themeIndex.value = index;
  router.push(item.path);
};
</script>

<style lang="scss" scoped>
.home {
  width: 100%;
  height: 100%;
  background-color: gray;
  main {
    position: relative;
    width: 100%;
    height: calc(100% - 170px);
    .el-carousel {
      height: 100%;
    }
    :deep(.el-carousel__container) {
      height: 100%;
      color: #fff;
    }
    .el-carousel__item {
      height: 100%;
      background-color: #d3dce6;
    }
    .el-carousel__item:nth-child(2n) {
      background-color: #99a9bf;
    }

    .el-carousel__item:nth-child(2n + 1) {
      background-color: #d3dce6;
    }
    .home_img {
      position: relative;
      width: 100%;
      height: 100%;
      background: url("../../assets/img/home.png");
      background-size: 100% 100%;
      .social_theme {
        position: absolute;
        bottom: 0px;
        left: 0px;
        display: flex;
        justify-content: space-around;
        width: 100%;
        height: 128px;
        background: rgba(255, 255, 255, 0.3);
        backdrop-filter: blur(4px);
        .theme {
          width: 370px;
          height: 94px;
          margin-top: 17px;
          line-height: 94px;
          text-align: center;
          border-radius: 8px;
          background: linear-gradient(
            110deg,
            rgba(55, 114, 255, 0.24) 0%,
            rgba(255, 255, 255, 0.75) 95%
          );
          border: 1px solid #ffffff;
          backdrop-filter: blur(10px);
          box-shadow: inset 0px 0px 4px 0px #ffffff;
          cursor: pointer;
          // ✅ 添加以下样式以支持换行
          white-space: normal;
          word-break: break-word;
          overflow-wrap: break-word;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          line-height: normal;
          padding: 10px;
        }
        .themeHighlight {
          background: linear-gradient(
            110deg,
            rgba(39, 174, 96, 0.74) 0%,
            rgba(255, 255, 255, 0.75) 95%
          );
        }
      }
    }
    .weather_forecast {
      display: flex;
      justify-content: space-between;
      position: absolute;
      top: 0px;
      left: 0px;
      width: 300px;
      height: 50px;
      background: #ffffff7e;
      color: #a8dcfc;
      .left {
        width: 45%;
        height: 100%;
        > div {
          text-align: center;
          line-height: 25px;
        }
        .time {
          font-size: 20px;
          font-weight: 700;
        }
      }
      .right {
        display: flex;
        width: 48%;
        height: 100%;
        .tem {
          width: 70%;
          height: 100%;
          text-align: center;
          line-height: 50px;
          font-size: 20px;
          font-weight: 700;
        }
        .icon {
          width: 30%;
          height: 100%;
          background: url("../../assets/img/晴.png");
          background-size: 100% 100%;
        }
      }
    }
  }
  footer {
    width: 100%;
    height: 170px;
    .info {
      height: calc(100% - 30px);
      display: flex;
      background-color: #fff;
      .info_item {
        flex: 1;
        height: 100%;
        padding: 10px 30px;
        display: flex;
        flex-direction: column;
        gap: 5px;
        .title {
          height: 30px;
          font-size: 16px;
          font-weight: 600;
        }

        a {
          color: blue;
          text-decoration: underline;
          text-decoration-color: blue;
        }
        .content {
          // margin-right: 0px;
          width: 60%;
          height: calc(100% - 30px);
          display: flex;
          justify-content: center;
          align-items: center;
          gap: 5px;
          .item {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
          }
        }
      }
      .follow_us {
        flex: 1;
        height: 100%;
        padding: 10px 30px;
        display: flex;
        flex-direction: column;
        gap: 5px;
        .title {
          height: 30px;
          font-size: 16px;
          font-weight: 600;
          margin-left: 50px;
          margin-left: auto; // 添加这行，使content向右靠齐
          margin-right: 265px; // 添加这行，设置右侧间距
        }
        .content {
          // margin-right: 0px;
          width: 60%;
          height: calc(100% - 30px);
          display: flex;
          justify-content: center;
          align-items: center;
          gap: 5px;
          margin-left: auto; // 添加这行，使content向右靠齐
          margin-right: 30px; // 添加这行，设置右侧间距
          .item {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            &:first-child {
              margin-right: -20px; // 让第一个item向右移动
            }

            &:last-child {
              margin-left: -20px; // 让最后一个item向左移动
            }
            img {
              width: 80px;
              height: 80px;
            }
          }
        }
      }
    }
    .bottom {
      height: 30px;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #fff;
      font-size: 12px;
      background-color: #252525;
    }
  }
}
</style>
