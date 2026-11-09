<template>
  <div class="lay" >
      <div id="bg_topic" class="img-topic">
       
        <div class="container">
          <div class="row">
            <div class="content-column">
              <article>
                
                <div class="panel-topic">
                  <div class="panel-body">
                    <nav class="breadcrumb">
                      <a href="/">{{ navpage.home }}</a>
                    
                    </nav>
                    <h1 class="theme1-title" style="color: azure;">{{ navpage.theme1title }}</h1>
                  </div>

                </div>
              </article>
            </div>
          </div>
        </div>
      </div>
      

  <main class="main">
    <div class="container">
      <div class="main-content">
        
        <div>
          <h1></h1>
        </div>
        <div class="view-container">
          <div>
            <p>{{navpage.list}}</p>
          </div>
          <div class="article-list">
            
            <article>
              <hr>
              <h1></h1>
              <figure class="art-list-img">
                  <a class="img-ratio-169" href="/#/home/theme1/paper1">
                    <img loading="lazy" src="@/assets/img/login.png" width="300" height="160" alt="pic-4">
                  </a>
                </figure>
              <div class="article-list-content">
                <h2 class="article-list-title">
                  <a href="/#/home/theme1/paper1" class="art-title">{{navpage.arttitle1}}</a>
                </h2>
                <p class="article-list-summary" >
                  {{ navpage.artListSummary1 }}
                </p>
              </div>
            </article>
            
            <hr>
            <h1></h1>
            <article>
              <figure class="art-list-img">
                  <a class="img-ratio-169" href="/#/home/theme1/paper3">
                    <img loading="lazy" src="@/assets/img/login.png" width="300" height="160" alt="pic-4">
                  </a>
                </figure>
              <div class="article-list-content">
                <h2 class="article-list-title">
                  <a href="/#/home/theme1/paper3" class="art-title">{{navpage.arttitle2}}</a>
                </h2>
                <p class="article-list-summary" >
                 {{ navpage.artListSummary2 }}
                </p>
              </div>
            </article>
            <hr>
            <h1></h1>
            <article>
              <figure class="art-list-img">
                  <a class="img-ratio-169" href="/#/home/theme1/paper4">
                    <img loading="lazy" src="@/assets/img/login.png" width="300" height="160" alt="pic-4">
                  </a>
                </figure>
              <div class="article-list-content">
                <h2 class="article-list-title">
                  <a href="/#/home/theme1/paper4" class="art-title">{{ navpage.arttitle3 }}</a>
                </h2>
                <p class="article-list-summary" >
                  {{ navpage.artListSummary3 }}
                </p>
              </div>
            </article>
          </div>
        </div>
      </div>
    </div>
  </main>


  <footer>
    <hr>
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
import { useI18n } from "vue-i18n";
import { computed } from "vue";
const { locale } = useI18n();
const navpage = computed(()=>{
  if (locale.value === "chinese") {
    return {
    theme1title:"可再生能源资源利用现状与潜力洞察",
    arttitle2:"我国太阳能发电平价路径与并网潜力",
    arttitle3:"我国陆上风电跨区域联合并网及开发优化布局",
    arttitle1:"屋顶光伏的动态气候价值与潜力",
    list:"文章列表",
    home:"主页 / " ,
    topic:"主题 / ",
    artListSummary2:"近日，清华大学环境学院、碳中和研究院、哈佛大学等国内外联合团队在碳中和背景下我国太阳能光伏发电平价动态与并网潜力研究方面取得新进展，系统动态评估了我国技术可行、经济平价、电网兼容的光伏发电潜力，揭示了光伏成本优势下“光伏+储能”的广阔应用前景。该研究成果可为碳中和背景下的光伏发电资源开发、新型电力系统规划提供参考，对于光伏发电与终端用能“电气化”与储能技术融合，促进工业、建筑、交通等领域的脱碳具有启示意义。",
    artListSummary1:"近日，清华大学碳中和研究院院长助理、环境学院教授鲁玺课题组与美国密西根大学Michael T. Craig助理教授团队联合，在揭示屋顶光伏的动态气候价值与潜力实现方面取得新成果。该研究基于未来多维气候模式的气候变化路径结果，量化了气候变化对居民屋顶的光伏发电潜力与用能行为的双重影响，揭示了气候变化提升居民屋顶光伏使用价值并促进屋顶光伏投资的影响机制。该研究首次量化了气候变化对可再生能源应用价值的影响，对面向中长期的屋顶光伏开发布局与规划具有关键科学指导意义。",
    artListSummary3:"近日，清华大学环境学院鲁玺课题组在国际能源领域著名期刊《应用能源》（Applied Energy）发表题为《基于聚类分析的中国陆上风电开发优化布局研究》（Optimal allocation of onshore wind power in China based on cluster analysis）的研究论文。研究基于聚类分析方法首次对中国大陆风电资源的变动特性进行分区，并从风电时空变化特性的新视角探讨了风电资源规划开发策略，对碳中和目标下的风电等可再生能源的持续开发利用具有借鉴意义。"
}
}
  else{
    return {
      theme1title:" Current Status and Potential Insights of Renewable Energy Resource Utilization",
      arttitle1:"Dynamic Climate Value and Potential Realization of Rooftop Photovoltaics",
      arttitle2:"China's Solar Power Grid Parity Pathway and Grid Integration Potential",
      arttitle3:"Joint Inter-Regional Wind Power Grid and Deployment Optimization in China",
      list:"article list",
      home:"Home / ",
      topic:"Topic / ",
      artListSummary1:"Recently, the research team led by Prof. Xi Lu from the Institute for Carbon Neutrality and the School of Environment, Tsinghua University, in collaboration with Assistant Professor Michael T. Craig’s team from the University of Michigan, has made new progress in revealing the dynamic climate value and potential realization of rooftop photovoltaics. Based on the results of future multidimensional climate models, this study quantifies the dual impact of climate change on the photovoltaic generation potential on residential rooftops and energy consumption behavior, revealing the mechanism through which climate change enhances the value of residential rooftop photovoltaics and promotes investment in rooftop photovoltaics. This study quantifies for the first time the impact of climate change on the value of renewable energy applications, providing key scientific guidance for the medium to long-term development and planning of rooftop photovoltaics.",
      artListSummary2:"Recently, a joint research team from Tsinghua University's School of Environment, Carbon Neutrality Institute, Harvard University, and other domestic and international institutions made new progress in studying the dynamic grid parity and grid integration potential of solar photovoltaic (PV) power generation in China under the carbon neutrality framework. The study systematically and dynamically evaluates the technically feasible, economically competitive, and grid-compatible potential of PV power generation in China, revealing the broad application prospects of \"PV + storage\" under the cost advantage of PV. The research findings can serve as a reference for PV resource development and new power system planning in the context of carbon neutrality, offering insights into the integration of PV power generation with end-use electrification and energy storage technologies to promote decarbonization in industries, buildings, transportation, and other sectors.",
      artListSummary3:"SOE, Tsinghua, January 27th, 2021. Recently, Xi Lu, from SOE, Tsinghua, and his research group published a paper titled Optimal allocation of onshore wind power in China based on cluster analysis on Applied Energy, a leading international journal in the field of energy. The study is the first to apply cluster analysis to identify the variability characteristics of wind power in the Chinese mainland. It also discusses the planning and development strategies of wind power from a novel perspective of the spatial and temporal variability characteristics of resources, which has implications for the sustainable development and utilization of renewable energy sources such as wind power under the goal of carbon neutrality."
    }
  }
}
);
import { onMounted, onBeforeUnmount } from 'vue'
import { color } from "echarts";

import img1 from '@/assets/img/home.png';
</script>


<style lang="scss" scoped>
  .lay{
    width: 100%;
    height: 100%;
  }
  .img-topic{
    box-sizing: border-box;
    -webkit-text-size-adjust: 100%;
   background-image:url("@/assets/img/login.png");
    min-height: 360px;
    
  }
  .container{
    width: 90%;
    margin: 0 auto;
    min-width: 320px;
    padding: 2%;
  }
  .row{
    flex-wrap: wrap;
    margin: 0 -20px;
    box-sizing: border-box;
  }
  .content-column{
    width: 50%;
    height: 35%;
    padding: 0 20px;
    display: block;
    unicode-bidi: isolate;
  }
  .panel-topic{
    background: #19aada;
  }
  .panel-body{
    padding: 2rem;
    display: block;
    unicode-bidi: isolate;
  }
  .breadcrumb{
    opacity: .8;
  }
  .theme1-title{
    color: whitesmoke;
  }
.content{
  display: 0 !important;
  .nav-btns{
    display: 0 !important;
  }
}
.main{
    padding: 2rem 0;
    .view-container{
      width: 100%;
      height: 100%;
    }
    .container{
      padding: 0 20px;
      width: 100%;
      margin: 0 auto;
      min-width: 320px;
    }
    .main-content{
      display: block;
      unicode-bidi: isolate;
      width: 79%;
      margin-left: 15%;
      //margin-right: 15%;
      padding-left: 25px;
      //padding-right: 25px;
    }
   
    .article-list{
      margin-bottom: 1.5rem;
      //padding: 2px;
      width: 80%;//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
      padding-top: 10px;
     // margin-top: 5%;
    }
    
    .article-list-content{
      overflow: hidden;
      height: 100%;
      margin-left: 35%;
    }
    .article-list-title{
      max-width: 90%;
    }
    .article-list-summary{
      max-width: 90%;
      margin-top: 0.5rem;
      font-size: 16px;
    }
    .art-title{
      color: black;
    }
    .img-ratio-169{
      padding-top: 26.25%;
    }
    .art-list-img{
      width: 25%;
      float: left;
      margin: 0 30px 0 0;
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
  
</style>