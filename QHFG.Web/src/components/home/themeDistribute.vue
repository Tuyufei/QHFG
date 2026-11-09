<template>
<Theme :themeName="themeID" :themes="theme" :themeTitle="themeTitle" />
</template>

<script setup>
import Theme from '@/components/home/theme.vue'
import { useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { computed } from 'vue';

const { locale } = useI18n();

const route = useRoute();
const themeID = route.path.split('/')[2]
const themes = {
  theme1: [
    { 
      id: 1,
      name:{
        chinese: '屋顶光伏的动态气候价值与潜力',
        english: 'Dynamic climate value and potential of rooftop photovoltatics'
      },
      time: {
        chinese: '2024-04-22',
        english: '2024-04-22'
      },
      path: "/home/theme1/paper1"
    },
    { 
      id: 2,
      name:{
        chinese: '我国太阳能发电平价路径与并网潜力',
        english: 'Our country`s solar power parity path and grid connection potential'
      },
      time: {
        chinese: '2021-10-19',
        english: '2021-10-19'
      },
      path: "/home/theme1/paper2"
    },
    { 
      id: 3,
      name:{
        chinese: '我国陆上风电与跨区域联合并网及开发优化布局',
        english: 'Cross-regional joint grid connection and development optimization layout of onshore wind power in my country'
      },
      time: {
        chinese: '2021-10-19',
        english: '2021-10-19'
      },
      path: "/home/theme1/paper3"
    }
  ],
  theme2: [
    {
      id: 1,
      name: {
        chinese: '中国碳中和目标下的风电时空碳足迹及相关成本',
        english: 'Spatiotemporal Dynamic Assessment of Carbon Footprint and Cost Variability of Onshore Wind Power in China'
      },
      time: {
        chinese: '2024-04-23',
        english: '2024-04-23'
      },
      path: "/home/theme2/paper1"
    },
    {
      id: 2,
      name: {
        chinese: '碳中和目标下可再生能源发展布局',
        english: 'Development Layout of Renewable Energy Under Carbon Neutrality Goals'
      },
      time: {
        chinese: '2024-03-01',
        english: '2024-03-01'
      },
      path: "/home/theme2/paper2"
    },
    {
      id: 3,
      name: {
        chinese: '中国碳中和目标下的风光技术展望',
        english: 'Technology Outlook on Wind and Solar Power toward China’s Neutrality Goal'
      },
      time: {
        chinese: '2024-01-11',
        english: '2024-01-11'
      },
      path: "/home/theme2/paper3"
    },
    {
      id: 4,
      name: {
        chinese: '全球光伏产业链温室气体排放与减排的时空分布特征',
        english: 'Spatiotemporal Distribution Characteristics of Greenhouse Gas Emissions and Reduction in the Global Photovoltaic Industry Chain'
      },
      time: {
        chinese: '2023-10-12',
        english: '2023-10-12'
      },
      path: "/home/theme2/paper4"
    }
  ],
  theme3: [
    {
      id: 1,
      name: {
        chinese: "中国可再生能源发电内在的时空不确定性研究",
        english: "Inherent Spatiotemporal Uncertainty of Renewable Power in China"
      },
      time: {
        chinese: "2024-05-07",
        english: "2024-05-07"
      },
      path: "/home/theme3/paper1"
    },
    {
      id: 2,
      name: {
        chinese: "大规模山区风电场对京津冀地区冬季空气质量的影响",
        english: "Impacts of Large-Scale Deployment of Mountainous Wind Farms On Wintertime Regional Air Quality in the Beijing-Tian-Hebei Area"
      },
      time: {
        chinese: "2023-02-10",
        english: "2023-02-10"
      },
      path: "/home/theme3/paper2"
    },
    {
      id: 3,
      name: {
        chinese: "空气质量提升对光伏发电与碳中和目标的协同效益",
        english: "Synergistic Benefits of Air Quality Improvement on Photovoltaic Power Generation and Carbon Neutrality Goals"
      },
      time: {
        chinese: "2022-05-06",
        english: "2022-05-06"
      },
      path: "/home/theme3/paper3"
    }
  ]
};
const themeTitles = {
  theme1: {
    chinese: '主题一',
    english: 'Theme 1: Insight into the Current Status and Potential of Renewable Energy Resource Utilization',
    image: '/src/assets/img/home.png', // 直接使用相对路径
    description: {
      chinese: '本主题探讨可再生能源的现状与潜力...',
      english: 'This theme explores the current status and potential of renewable energy...'
    }
  },
  theme2: {
    chinese: '主题二',
    english: 'Theme 2: Development Pathways for Renewable Energy Under Carbon Neutrality Goals',
    image: '/src/assets/img/home.png', // 直接使用相对路径
    description: {
      chinese: '本主题探讨可再生能源的现状与潜力...',
      english: 'This theme explores the current status and potential of renewable energy...'
    }
  },
  theme3: {
    chinese: '主题三',
    english: 'Theme 3: Risks and Resilience Enhancement in High-Proportion Renewable Energy Systems',
    image: '/src/assets/img/home.png', // 直接使用相对路径
    description: {
      chinese: '本主题探讨可再生能源的现状与潜力...',
      english: 'This theme explores the current status and potential of renewable energy...'
    }
  }
};
const themeTitle = computed(() => {
  return themeTitles[themeID]?.[locale.value] || '';
});

// 使 theme 响应 locale 变化
const theme = computed(() => {
  return themes[themeID]?.map(item => {
    if ((themeID === 'theme3' || themeID === 'theme2'||themeID === 'theme1') && item.name && item.time) {
      return {
        ...item,
        name: locale.value === 'english' ? item.name.english : item.name.chinese,
        time: locale.value === 'english' ? item.time.english : item.time.chinese
      }
    }
    return item;
  })
})
</script>
