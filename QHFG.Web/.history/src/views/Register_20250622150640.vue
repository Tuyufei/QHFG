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
              <el-input
                v-model="form.phone"
                :placeholder="$t('register.phonePlaceholder')"
                autocomplete="off"
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
                <el-option label="学生" value="学生" />
                <el-option label="教师" value="教师" />
                <el-option label="其他人员" value="其他人员" />
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

const { t } = useI18n();
const router = useRouter();
const registerForm = ref(null);
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
});


const codeBtnTextContent = ref("获取验证码");
const codeBtnDisabled = ref(false);
let timer = null;
let count = 6; // 倒计时秒数


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
