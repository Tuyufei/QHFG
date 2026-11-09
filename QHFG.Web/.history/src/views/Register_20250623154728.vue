<template>
  <div class="register">
    <div class="module_control">
      <div class="control_header">
        <span>{{ $t("message.systemTitle") }}</span>
      </div>
      <div class="control_body">
        <div class="right_form">
          <div class="register_title">
            ——
            <span class="title_label">{{ $t("register.registerPage") }}</span>
            ——
          </div>
          <el-form
            :model="form"
            ref="registerForm"
            label-width="auto"
            style="max-width: 600px"
          >
            <el-form-item
              :label="$t('register.userName')"
              prop="username"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.userNameTip'),
                  trigger: 'blur',
                  min:4,
                },
              ]"
            >
              <el-input
                v-model="form.username"
                :placeholder="$t('register.userNamePlaceholder')"
              />
            </el-form-item>
            <!-- 密码 -->
            <el-form-item
              :label="$t('register.password')"
              prop="password"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.passwordTip'),
                  trigger: 'blur',
                  min:6,
                },
              ]"
            >
              <el-input
                v-model="form.password"
                type="password"
                :placeholder="$t('register.password')"
                show-password
              />
            </el-form-item>
            <!-- 确认密码 -->
            <el-form-item
              :label="$t('register.password_confirm')"
              prop="password_confirm"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.passwordTip_confirm'),
                  trigger: 'blur',
                  validator: (rule, value, callback) => {
                    if (value !== form.password) {
                      callback(new Error('两次输入的密码不一致'));
                    } else {
                      callback();
                    }
                  },
                },
              ]"
            >
              <el-input
                v-model="form.password_confirm"
                type="password"
                :placeholder="$t('register.password_confirm')"
                show-password
              />
            </el-form-item>
            <!-- 真实姓名 -->
            <el-form-item
              :label="$t('register.realName')"
              prop="fullName"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.realNameTip'),
                  trigger: 'blur',
                },
              ]"
            >
              <el-input
                v-model="form.fullName"
                :placeholder="$t('register.realName')"
              />
            </el-form-item>
            <!-- 手机号 -->
            <el-form-item
              :label="$t('register.phone')"
              prop="phone"
              :rules="rules.phone"
            >
              <el-select
                v-model="form.countryCode"
                style="width: 160px; margin-right: 8px"
                placeholder="选择国家/地区"
              >
                <el-option
                  v-for="item in countryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-input
                v-model="form.phone"
                :placeholder="$t('register.phonePlaceholder')"
                autocomplete="off"
                style="width: 200px"
              />
            </el-form-item>
            <!-- 邮箱 -->
            <el-form-item
              :label="$t('register.email')"
              prop="email"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.emailTip1'),
                  trigger: 'blur',
                },
                {
                  type: 'email',
                  message: () => $t('register.emailTip2'),
                  trigger: 'blur',
                },
              ]"
            >
              <el-input
                v-model="form.email"
                :placeholder="$t('register.emailPlaceholder')"
              />
            </el-form-item>
            <!-- 单位 -->
            <el-form-item
              :label="$t('register.unit')"
              prop="company"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.unitTip'),
                  trigger: 'blur',
                  pattern: /^[\u4e00-\u9fa5a-zA-Z]+$/, 
                  min:4,
                  // validateNoRepeating = (rule, value, callback) => {
                  //   if (!value) return callback();
                    
                  //   // 检查是否存在长度≥4的重复子串
                  //   for (let i = 0; i < value.length - 3; i++) {
                  //     const char = value[i];
                  //     if (
                  //       value[i + 1] === char &&
                  //       value[i + 2] === char &&
                  //       value[i + 3] === char
                  //     ) {
                  //       return callback(new Error('错误'));
                  //     }
                  //   }
                    
                  //   callback();
                  // },
                },
              ]"
            >
              <el-input
                v-model="form.company"
                :placeholder="$t('register.unitPlaceholder')"
              />
            </el-form-item>
            <!-- 身份 -->
            <el-form-item
              :label="$t('register.identity')"
              prop="region"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.identityTip'),
                  trigger: 'blur',
                },
              ]"
            >
              <el-select
                v-model="form.region"
                :placeholder="$t('register.identityPlaceholder')"
                style="width: 100%;"
              >
                <el-option v-for="(item, index) in identityOptions" :key="index" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item
              :label="$t('register.identifyingCode')"
              prop="code"
              :rules="[
                {
                  required: true,
                  message: () => $t('register.identifyingCodeTip'),
                  trigger: 'blur',
                },
              ]"
            >
              <el-input
                v-model="form.code"
                :placeholder="$t('register.identifyingCodePlaceholder')"
                style="width: 200px; margin-right: 10px"
              />
              <el-button :disabled="codeBtnDisabled" @click="getEmailCode">{{
                codeBtnText
              }}</el-button>
            </el-form-item>
          </el-form>
          <div class="register_btn" @click="register">
            {{ $t("register.register") }}
          </div>
        </div>
      </div>
      <div class="to_login">
        {{ $t("register.hasAccount") }}
        <span @click="gotoLogin">{{ $t("register.login") }} →</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { registerInterface, getEmailCodeInterface } from "@/request";
import { useI18n } from "vue-i18n";
// 使用计算属性生成选项列表
const identityOptions = computed(() => [
  { label: t('register.identity1'), value: t('register.identity1') },
  { label: t('register.identity2'), value: t('register.identity2') },
  { label: t('register.identity3'), value: t('register.identity3') },
  { label: t('register.identity4'), value: t('register.identity4') },
  { label: t('register.identity5'), value: t('register.identity5') },
  { label: t('register.identity6'), value: t('register.identity6') },
  { label: t('register.identity7'), value: t('register.identity7') }
])
const { t } = useI18n();
const router = useRouter();
const registerForm = ref(null);
const countryOptions = [
  { label: "东帝汶 (+670)", value: "+670" },
  { label: "中国 (+86)", value: "+86" },
  { label: "丹麦 (+45)", value: "+45" },
  { label: "乌克兰 (+380)", value: "+380" },
  { label: "乌拉圭 (+598)", value: "+598" },
  { label: "也门 (+967)", value: "+967" },
  { label: "以色列 (+972)", value: "+972" },
  { label: "伊拉克 (+964)", value: "+964" },
  { label: "伊朗 (+98)", value: "+98" },
  { label: "伯利兹 (+501)", value: "+501" },
  { label: "佛得角 (+238)", value: "+238" },
  { label: "俄罗斯 (+7)", value: "+7" },
  { label: "保加利亚 (+359)", value: "+359" },
  { label: "克罗地亚 (+385)", value: "+385" },
  { label: "冈比亚 (+220)", value: "+220" },
  { label: "冰岛 (+354)", value: "+354" },
  { label: "几内亚 (+224)", value: "+224" },
  { label: "几内亚比绍 (+245)", value: "+245" },
  { label: "刚果（布）(+242)", value: "+242" },
  { label: "刚果（金）(+243)", value: "+243" },
  { label: "利比亚 (+218)", value: "+218" },
  { label: "利比里亚 (+231)", value: "+231" },
  { label: "加拿大 (+1)", value: "+1" },
  { label: "加纳 (+233)", value: "+233" },
  { label: "加蓬 (+241)", value: "+241" },
  { label: "南极洲 (+672)", value: "+672" },
  { label: "南非 (+27)", value: "+27" },
  { label: "卡塔尔 (+974)", value: "+974" },
  { label: "印度 (+91)", value: "+91" },
  { label: "印度尼西亚 (+62)", value: "+62" },
  { label: "危地马拉 (+502)", value: "+502" },
  { label: "厄瓜多尔 (+593)", value: "+593" },
  { label: "厄立特里亚 (+291)", value: "+291" },
  { label: "叙利亚 (+963)", value: "+963" },
  { label: "古巴 (+53)", value: "+53" },
  { label: "吉布提 (+253)", value: "+253" },
  { label: "哥斯达黎加 (+506)", value: "+506" },
  { label: "喀麦隆 (+237)", value: "+237" },
  { label: "图瓦卢 (+688)", value: "+688" },
  { label: "土耳其 (+90)", value: "+90" },
  { label: "圣卢西亚 (+1-758)", value: "+1-758" },
  { label: "圣基茨和尼维斯 (+1-869)", value: "+1-869" },
];
// 按照 label 的首字母排序（中文拼音）
countryOptions.sort((a, b) => a.label.localeCompare(b.label, 'zh-Hans-CN'));

const form = reactive({
  username: "",
  password: "",
  password_confirm: "",
  fullName: "",
  phone: "",
  email: "",
  company: "",
  region: "",
  code: "",
  countryCode: "+86", // 默认中国
  phone: "",
});




const codeBtnTextContent = ref("获取验证码");
const codeBtnDisabled = ref(false);
let timer = null;
let count = 60; // 倒计时秒数


const codeBtnText = computed(() => {
  if (codeBtnDisabled.value) {
    // if (timer) {
    //   console.log("codeBtnTextContent.value:",codeBtnTextContent.value)
    //   return codeBtnTextContent.value;
    // }
    codeBtnTextContent.value = t("register.code.retryAfter", { count });
    return codeBtnTextContent.value;
  }
  return t("register.code.getCode");
});






// 获取邮箱验证码
const getEmailCode = async () => {
  if (!form.email) {
    ElMessage.warning(t("register.code.emailRequired"));
    return;
  }
  codeBtnDisabled.value = true;
  try {
    await getEmailCodeInterface(form.email);
    ElMessage.success(t("register.code.codeSent"));
    console.log("count:",count);
    codeBtnTextContent.value = t("register.code.retryAfter", { count });
    timer = setInterval(() => {
      count--;
      console.log("count:",count);
      codeBtnTextContent.value = t("register.code.retryAfter", { count });

      if (count <= 0) {
        clearInterval(timer);
        timer = null;
        codeBtnDisabled.value = false;
      }
    }, 1000);
  } catch (e) {
    ElMessage.error(t("register.code.sendFailed"));
    codeBtnDisabled.value = false;
  }
};

// 手机号验证函数
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error(t("register.phoneTip")));
    return;
  }

  // 移除所有空格和连字符
  const cleanNumber = value.replace(/[\s-]/g, "");

  // 检查是否以+开头
  if (cleanNumber.startsWith("+")) {
    // 国际号码格式
    if (!/^\+\d{1,4}\d{10,15}$/.test(cleanNumber)) {
      callback(new Error(t("register.phoneInternationalTip")));
      return;
    }
  } else {
    // 国内号码格式
    if (!/^1[3-9]\d{9}$/.test(cleanNumber)) {
      callback(new Error(t("register.phoneLocalTip")));
      return;
    }
  }
  callback();
};

// 在表单规则中使用
const rules = {
  phone: [
    { required: true, message: () => t("register.phoneTip"), trigger: "blur" },
    { validator: validatePhone, trigger: "blur" },
  ],
};

// 注册
const register = () => {
  console.log("!!!!!!!!!!!!!!!!")
  registerForm.value.validate(async (valid) => {
    console.log("valid:",valid)
    if (!valid) return;
    // 拼接完整手机号
    const fullPhone = form.countryCode + form.phone;
    // 传 fullPhone 给后端
    const res = await registerInterface(
      form.username,
      form.password,
      form.password_confirm,
      form.fullName,
      fullPhone, // 这里用拼接后的手机号
      form.email,
      form.company,
      form.region,
      form.code
    );
    try {
      const res = await registerInterface(
        form.username,
        form.password,
        form.password_confirm,
        form.fullName,
        form.phone,
        form.email,
        form.company,
        form.region,
        form.code
      );
      console.log("res11", res);
      if (res.code === 200) {
        // 如果注册成功返回了 token，也保存它
        localStorage.setItem(
          "leiyangUser",
          JSON.stringify({
            username: form.username,
            token: res.token, // 如果注册接口返回 token
          })
        );
        ElMessage.success(t("register.registerSuccess"));
        router.push("/home");
      } else {
        const errorMessages = {
          411: t("register.error.codeError"),
          412: t("register.error.usernameExists"),
          413: t("register.error.emailExists"),
          414: t("register.error.phoneExists"),
          419: t("register.error.registerFailed"),
        };

        if (errorMessages[res.code]) {
          ElMessage.error(errorMessages[res.code]);
          return;
        }
      }
    } catch (error) {
      console.log("error", error);
      ElMessage.error(error.msg || t("register.error.registerFailed"));
    }
  });
};

const gotoLogin = () => {
  router.push("/home/login");
};
</script>

<style lang="scss" scoped>
.register {
  position: relative;
  width: 100%;
  // height: 100vh;
  height: calc(100vh - 70px);
  background-image: url("@/assets/img/register.png");
  background-size: cover;
  background-position: center;
  display: flex;
  justify-content: center;
  align-items: center;

  .module_control {
    width: 570px;
    background-color: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
    padding: 30px;

    .control_header {
      text-align: center;
      font-size: 24px;
      font-weight: bold;
      color: #333;
      margin-bottom: 30px;
    }

    .control_body {
      .right_form {
        .register_title {
          text-align: center;
          margin-bottom: 30px;
          color: #666;
          font-size: 20px;

          .title_label {
            color: #409eff;
            font-weight: bold;
          }
        }

        .el-form {
          margin-bottom: 20px;
        }

        .register_btn {
          width: 100%;
          height: 40px;
          line-height: 40px;
          text-align: center;
          background-color: #409eff;
          color: white;
          border-radius: 20px;
          cursor: pointer;
          transition: all 0.3s;
          font-size: 16px;

          &:hover {
            background-color: #66b1ff;
          }
        }
      }
    }

    .to_login {
      text-align: center;
      margin-top: 20px;
      color: #666;

      span {
        color: #409eff;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}
</style>
