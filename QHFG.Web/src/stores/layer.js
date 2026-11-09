import { ref, computed, shallowRef } from "vue";
import { defineStore } from "pinia";

export const useLayerStore = defineStore(
  "layer",
  () => {
    // 图层列表
    const layerList = shallowRef([]);
    // 设置图层列表
    const setLayerList = val => {
      layerList.value = val;
    };
    // 选中的图层列表
    const selectedLayerList = shallowRef([]);
    // 设置选中的图层列表
    const setSelectedLayerList = val => {
      selectedLayerList.value = val;
    };
    // 行政区数据
    const regionList = shallowRef([]);
    // 设置行政区划数据
    const setRegionList = val => {
      regionList.value = val;
    };
    // 字典列表
    const dicList = shallowRef([]);
    // 设置字典列表
    const setDicList = val => {
      dicList.value = val;
    };
    return {
      layerList,
      setLayerList,
      selectedLayerList,
      setSelectedLayerList,
      regionList,
      setRegionList,
      dicList,
      setDicList,
    };
  }
  // {
  //   persist: true, //持久化
  // }
);
