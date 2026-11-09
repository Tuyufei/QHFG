// 封装axios
import axios from "axios";
// import { QGIS_Config } from "@/config/qgis";
const baseURL = "http://129.211.174.149:18080";
// const baseURL = "http://172.27.160.161:8080";
// baseURL: QGIS_Config.SqlUrl,
const request = axios.create({
  baseURL: baseURL,
  timeout: 5000,
});
// 设置请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const userInfo = localStorage.getItem("leiyangUser");
    let token = "";
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo);
        token = user.token;
      } catch (e) {
        console.error("解析用户信息失败:", e);
      }
    }

    // 设置请求头
    config.headers = {
      ...config.headers,
      "Content-Type": "application/json;charset=UTF-8",
      token: token, // 添加 token 到请求头
    };

    // 如果是 GET 请求，确保参数正确设置
    if (config.method === "get") {
      // 确保params对象存在
      config.params = {
        ...config.params,
      };
    }
    // console.log("config: ",config)
    // 打印请求信息，方便调试
    // console.log('Request Config:', {
    //   url: config.url,
    //   method: config.method,
    //   params: config.params,
    //   data: config.data
    // });

    return config;
  },
  (error) => {
    // console.log(error)
    return Promise.reject(error);
  }
);

// 定义响应拦截器
request.interceptors.response.use(
  (res) => {
    // console.log(res)
    if (res.status === 200) {
      // 如果响应中包含新的 token，更新本地存储
      if (res.data && res.data.token) {
        const userInfo = localStorage.getItem("leiyangUser");
        if (userInfo) {
          try {
            const user = JSON.parse(userInfo);
            user.token = res.data.token;
            localStorage.setItem("leiyangUser", JSON.stringify(user));
          } catch (e) {
            console.error("更新 token 失败:", e);
          }
        }
      }
      return res.data;
    }
    throw Error("请求数据失败");
  },
  (err) => {
    // 如果是 401 错误（未授权），清除用户信息并跳转到登录页
    if (err.response && err.response.status === 401) {
      localStorage.removeItem("leiyangUser");
      window.location.href = "/#/home/login";
    }
    return Promise.reject(err);
  }
);
export default request;
