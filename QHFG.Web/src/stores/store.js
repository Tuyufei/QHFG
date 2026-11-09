// store.js
import { createStore } from 'vuex';

const store = createStore({
  state: {
    pathUrl: '', // 定义 pathUrl
  },
  mutations: {
    setPathUrl(state, pathUrl) {
      state.pathUrl = pathUrl; // 更新 pathUrl
    },
  },
  actions: {
    updatePathUrl({ commit }, pathUrl) {
      commit('setPathUrl', pathUrl); // 触发 mutation 更新 pathUrl
    },
  },
  getters: {
    getPathUrl(state) {
      return state.pathUrl; // 获取 pathUrl
    },
  },
});
export default store;