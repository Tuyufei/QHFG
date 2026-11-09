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
                  validator: (rule, value, callback) => {
                    if (!value) return callback();
                    
                    // 检查是否存在长度≥4的重复子串
                    for (let i = 0; i < value.length - 3; i++) {
                      const char = value[i];
                      if (
                        value[i + 1] === char &&
                        value[i + 2] === char &&
                        value[i + 3] === char
                      ) {
                        return callback(new Error('错误'));
                      }
                    }
                    callback();
                  },
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

let countryOptions = [
  { label: "东帝汶 (+670)", value: "+670" },
  { label: t('register.dui '), value: "+86" },
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
  { label: "阿尔及利亚 (+213)", value: "+213" },
  { label: "阿尔巴尼亚 (+355)", value: "+355" },
  { label: "阿富汗 (+93)", value: "+93" },
  { label: "阿根廷 (+54)", value: "+54" },
  { label: "阿拉伯联合酋长国 (+971)", value: "+971" },
  { label: "阿曼 (+968)", value: "+968" },
  { label: "埃及 (+20)", value: "+20" },
  { label: "埃塞俄比亚 (+251)", value: "+251" },
  { label: "爱尔兰 (+353)", value: "+353" },
  { label: "爱沙尼亚 (+372)", value: "+372" },
  { label: "安哥拉 (+244)", value: "+244" },
  { label: "安提瓜和巴布达 (+1-268)", value: "+1-268" },
  { label: "奥地利 (+43)", value: "+43" },
  { label: "澳大利亚 (+61)", value: "+61" },
  { label: "巴哈马 (+1-242)", value: "+1-242" },
  { label: "巴基斯坦 (+92)", value: "+92" },
  { label: "巴巴多斯 (+1-246)", value: "+1-246" },
  { label: "巴布亚新几内亚 (+675)", value: "+675" },
  { label: "巴拿马 (+507)", value: "+507" },
  { label: "巴林 (+973)", value: "+973" },
  { label: "巴勒斯坦 (+970)", value: "+970" },
  { label: "巴西 (+55)", value: "+55" },
  { label: "白俄罗斯 (+375)", value: "+375" },
  { label: "保加利亚 (+359)", value: "+359" },
  { label: "比利时 (+32)", value: "+32" },
  { label: "冰岛 (+354)", value: "+354" },
  { label: "波兰 (+48)", value: "+48" },
  { label: "波斯尼亚和黑塞哥维那 (+387)", value: "+387" },
  { label: "玻利维亚 (+591)", value: "+591" },
  { label: "伯利兹 (+501)", value: "+501" },
  { label: "朝鲜 (+850)", value: "+850" },
  { label: "丹麦 (+45)", value: "+45" },
  { label: "德国 (+49)", value: "+49" },
  { label: "东帝汶 (+670)", value: "+670" },
  { label: "多哥 (+228)", value: "+228" },
  { label: "多米尼加共和国 (+1-809)", value: "+1-809" },
  { label: "多米尼克 (+1-767)", value: "+1-767" },
  { label: "厄瓜多尔 (+593)", value: "+593" },
  { label: "厄立特里亚 (+291)", value: "+291" },
  { label: "俄罗斯 (+7)", value: "+7" },
  { label: "法国 (+33)", value: "+33" },
  { label: "芬兰 (+358)", value: "+358" },
  { label: "菲律宾 (+63)", value: "+63" },
  { label: "佛得角 (+238)", value: "+238" },
  { label: "刚果（金）(+243)", value: "+243" },
  { label: "刚果（布）(+242)", value: "+242" },
  { label: "哥斯达黎加 (+506)", value: "+506" },
  { label: "格林纳达 (+1-473)", value: "+1-473" },
  { label: "格鲁吉亚 (+995)", value: "+995" },
  { label: "古巴 (+53)", value: "+53" },
  { label: "瓜地马拉 (+502)", value: "+502" },
  { label: "圭亚那 (+592)", value: "+592" },
  { label: "哈萨克斯坦 (+7)", value: "+7" },
  { label: "海地 (+509)", value: "+509" },
  { label: "韩国 (+82)", value: "+82" },
  { label: "荷兰 (+31)", value: "+31" },
  { label: "黑山 (+382)", value: "+382" },
  { label: "洪都拉斯 (+504)", value: "+504" },
  { label: "吉布提 (+253)", value: "+253" },
  { label: "几内亚 (+224)", value: "+224" },
  { label: "几内亚比绍 (+245)", value: "+245" },
  { label: "加拿大 (+1)", value: "+1" },
  { label: "加纳 (+233)", value: "+233" },
  { label: "加蓬 (+241)", value: "+241" },
  { label: "柬埔寨 (+855)", value: "+855" },
  { label: "捷克 (+420)", value: "+420" },
  { label: "津巴布韦 (+263)", value: "+263" },
  { label: "喀麦隆 (+237)", value: "+237" },
  { label: "卡塔尔 (+974)", value: "+974" },
  { label: "科摩罗 (+269)", value: "+269" },
  { label: "科特迪瓦 (+225)", value: "+225" },
  { label: "科威特 (+965)", value: "+965" },
  { label: "克罗地亚 (+385)", value: "+385" },
  { label: "拉脱维亚 (+371)", value: "+371" },
  { label: "黎巴嫩 (+961)", value: "+961" },
  { label: "立陶宛 (+370)", value: "+370" },
  { label: "利比亚 (+218)", value: "+218" },
  { label: "利比里亚 (+231)", value: "+231" },
  { label: "列支敦士登 (+423)", value: "+423" },
  { label: "卢森堡 (+352)", value: "+352" },
  { label: "卢旺达 (+250)", value: "+250" },
  { label: "罗马尼亚 (+40)", value: "+40" },
  { label: "马达加斯加 (+261)", value: "+261" },
  { label: "马尔代夫 (+960)", value: "+960" },
  { label: "马耳他 (+356)", value: "+356" },
  { label: "马拉维 (+265)", value: "+265" },
  { label: "马来西亚 (+60)", value: "+60" },
  { label: "马里 (+223)", value: "+223" },
  { label: "马绍尔群岛 (+692)", value: "+692" },
  { label: "毛里求斯 (+230)", value: "+230" },
  { label: "毛里塔尼亚 (+222)", value: "+222" },
  { label: "美国 (+1)", value: "+1" },
  { label: "蒙古 (+976)", value: "+976" },
  { label: "孟加拉 (+880)", value: "+880" },
  { label: "秘鲁 (+51)", value: "+51" },
  { label: "缅甸 (+95)", value: "+95" },
  { label: "摩尔多瓦 (+373)", value: "+373" },
  { label: "摩洛哥 (+212)", value: "+212" },
  { label: "摩纳哥 (+377)", value: "+377" },
  { label: "纳米比亚 (+264)", value: "+264" },
  { label: "纳鲁 (+674)", value: "+674" },
  { label: "南非 (+27)", value: "+27" },
  { label: "南极洲 (+672)", value: "+672" },
  { label: "尼泊尔 (+977)", value: "+977" },
  { label: "尼加拉瓜 (+505)", value: "+505" },
  { label: "尼日利亚 (+234)", value: "+234" },
  { label: "挪威 (+47)", value: "+47" },
  { label: "葡萄牙 (+351)", value: "+351" },
  { label: "日本 (+81)", value: "+81" },
  { label: "瑞典 (+46)", value: "+46" },
  { label: "瑞士 (+41)", value: "+41" },
  { label: "萨尔瓦多 (+503)", value: "+503" },
  { label: "萨摩亚 (+685)", value: "+685" },
  { label: "塞尔维亚 (+381)", value: "+381" },
  { label: "塞拉利昂 (+232)", value: "+232" },
  { label: "塞内加尔 (+221)", value: "+221" },
  { label: "塞浦路斯 (+357)", value: "+357" },
  { label: "塞舌尔 (+248)", value: "+248" },
  { label: "沙特阿拉伯 (+966)", value: "+966" },
  { label: "圣多美和普林西比 (+239)", value: "+239" },
  { label: "圣基茨和尼维斯 (+1-869)", value: "+1-869" },
  { label: "圣卢西亚 (+1-758)", value: "+1-758" },
  { label: "圣文森特和格林纳丁斯 (+1-784)", value: "+1-784" },
  { label: "斯里兰卡 (+94)", value: "+94" },
  { label: "斯洛文尼亚 (+386)", value: "+386" },
  { label: "苏丹 (+249)", value: "+249" },
  { label: "苏里南 (+597)", value: "+597" },
  { label: "索马里 (+252)", value: "+252" },
  { label: "塔吉克斯坦 (+992)", value: "+992" },
  { label: "汤加 (+676)", value: "+676" },
  { label: "特立尼达和多巴哥 (+1-868)", value: "+1-868" },
  { label: "突尼斯 (+216)", value: "+216" },
  { label: "图瓦卢 (+688)", value: "+688" },
  { label: "土耳其 (+90)", value: "+90" },
  { label: "乌克兰 (+380)", value: "+380" },
  { label: "乌拉圭 (+598)", value: "+598" },
  { label: "委内瑞拉 (+58)", value: "+58" },
  { label: "文莱 (+673)", value: "+673" },
  { label: "乌兹别克斯坦 (+998)", value: "+998" },
  { label: "西班牙 (+34)", value: "+34" },
  { label: "希腊 (+30)", value: "+30" },
  { label: "新加坡 (+65)", value: "+65" },
  { label: "新西兰 (+64)", value: "+64" },
  { label: "匈牙利 (+36)", value: "+36" },
  { label: "叙利亚 (+963)", value: "+963" },
  { label: "牙买加 (+1-876)", value: "+1-876" },
  { label: "亚美尼亚 (+374)", value: "+374" },
  { label: "也门 (+967)", value: "+967" },
  { label: "伊拉克 (+964)", value: "+964" },
  { label: "伊朗 (+98)", value: "+98" },
  { label: "以色列 (+972)", value: "+972" },
  { label: "意大利 (+39)", value: "+39" },
  { label: "印度 (+91)", value: "+91" },
  { label: "印度尼西亚 (+62)", value: "+62" },
  { label: "英国 (+44)", value: "+44" },
  { label: "约旦 (+962)", value: "+962" },
  { label: "越南 (+84)", value: "+84" },
  { label: "赞比亚 (+260)", value: "+260" },
  { label: "乍得 (+235)", value: "+235" },
  { label: "智利 (+56)", value: "+56" },
  { label: "中非共和国 (+236)", value: "+236" }
];
const countryOptionsZh = [
  { label: "东帝汶 (+670)", value: "+670" },
  { label: t('register.dui '), value: "+86" },
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
  { label: "阿尔及利亚 (+213)", value: "+213" },
  { label: "阿尔巴尼亚 (+355)", value: "+355" },
  { label: "阿富汗 (+93)", value: "+93" },
  { label: "阿根廷 (+54)", value: "+54" },
  { label: "阿拉伯联合酋长国 (+971)", value: "+971" },
  { label: "阿曼 (+968)", value: "+968" },
  { label: "埃及 (+20)", value: "+20" },
  { label: "埃塞俄比亚 (+251)", value: "+251" },
  { label: "爱尔兰 (+353)", value: "+353" },
  { label: "爱沙尼亚 (+372)", value: "+372" },
  { label: "安哥拉 (+244)", value: "+244" },
  { label: "安提瓜和巴布达 (+1-268)", value: "+1-268" },
  { label: "奥地利 (+43)", value: "+43" },
  { label: "澳大利亚 (+61)", value: "+61" },
  { label: "巴哈马 (+1-242)", value: "+1-242" },
  { label: "巴基斯坦 (+92)", value: "+92" },
  { label: "巴巴多斯 (+1-246)", value: "+1-246" },
  { label: "巴布亚新几内亚 (+675)", value: "+675" },
  { label: "巴拿马 (+507)", value: "+507" },
  { label: "巴林 (+973)", value: "+973" },
  { label: "巴勒斯坦 (+970)", value: "+970" },
  { label: "巴西 (+55)", value: "+55" },
  { label: "白俄罗斯 (+375)", value: "+375" },
  { label: "保加利亚 (+359)", value: "+359" },
  { label: "比利时 (+32)", value: "+32" },
  { label: "冰岛 (+354)", value: "+354" },
  { label: "波兰 (+48)", value: "+48" },
  { label: "波斯尼亚和黑塞哥维那 (+387)", value: "+387" },
  { label: "玻利维亚 (+591)", value: "+591" },
  { label: "伯利兹 (+501)", value: "+501" },
  { label: "朝鲜 (+850)", value: "+850" },
  { label: "丹麦 (+45)", value: "+45" },
  { label: "德国 (+49)", value: "+49" },
  { label: "东帝汶 (+670)", value: "+670" },
  { label: "多哥 (+228)", value: "+228" },
  { label: "多米尼加共和国 (+1-809)", value: "+1-809" },
  { label: "多米尼克 (+1-767)", value: "+1-767" },
  { label: "厄瓜多尔 (+593)", value: "+593" },
  { label: "厄立特里亚 (+291)", value: "+291" },
  { label: "俄罗斯 (+7)", value: "+7" },
  { label: "法国 (+33)", value: "+33" },
  { label: "芬兰 (+358)", value: "+358" },
  { label: "菲律宾 (+63)", value: "+63" },
  { label: "佛得角 (+238)", value: "+238" },
  { label: "刚果（金）(+243)", value: "+243" },
  { label: "刚果（布）(+242)", value: "+242" },
  { label: "哥斯达黎加 (+506)", value: "+506" },
  { label: "格林纳达 (+1-473)", value: "+1-473" },
  { label: "格鲁吉亚 (+995)", value: "+995" },
  { label: "古巴 (+53)", value: "+53" },
  { label: "瓜地马拉 (+502)", value: "+502" },
  { label: "圭亚那 (+592)", value: "+592" },
  { label: "哈萨克斯坦 (+7)", value: "+7" },
  { label: "海地 (+509)", value: "+509" },
  { label: "韩国 (+82)", value: "+82" },
  { label: "荷兰 (+31)", value: "+31" },
  { label: "黑山 (+382)", value: "+382" },
  { label: "洪都拉斯 (+504)", value: "+504" },
  { label: "吉布提 (+253)", value: "+253" },
  { label: "几内亚 (+224)", value: "+224" },
  { label: "几内亚比绍 (+245)", value: "+245" },
  { label: "加拿大 (+1)", value: "+1" },
  { label: "加纳 (+233)", value: "+233" },
  { label: "加蓬 (+241)", value: "+241" },
  { label: "柬埔寨 (+855)", value: "+855" },
  { label: "捷克 (+420)", value: "+420" },
  { label: "津巴布韦 (+263)", value: "+263" },
  { label: "喀麦隆 (+237)", value: "+237" },
  { label: "卡塔尔 (+974)", value: "+974" },
  { label: "科摩罗 (+269)", value: "+269" },
  { label: "科特迪瓦 (+225)", value: "+225" },
  { label: "科威特 (+965)", value: "+965" },
  { label: "克罗地亚 (+385)", value: "+385" },
  { label: "拉脱维亚 (+371)", value: "+371" },
  { label: "黎巴嫩 (+961)", value: "+961" },
  { label: "立陶宛 (+370)", value: "+370" },
  { label: "利比亚 (+218)", value: "+218" },
  { label: "利比里亚 (+231)", value: "+231" },
  { label: "列支敦士登 (+423)", value: "+423" },
  { label: "卢森堡 (+352)", value: "+352" },
  { label: "卢旺达 (+250)", value: "+250" },
  { label: "罗马尼亚 (+40)", value: "+40" },
  { label: "马达加斯加 (+261)", value: "+261" },
  { label: "马尔代夫 (+960)", value: "+960" },
  { label: "马耳他 (+356)", value: "+356" },
  { label: "马拉维 (+265)", value: "+265" },
  { label: "马来西亚 (+60)", value: "+60" },
  { label: "马里 (+223)", value: "+223" },
  { label: "马绍尔群岛 (+692)", value: "+692" },
  { label: "毛里求斯 (+230)", value: "+230" },
  { label: "毛里塔尼亚 (+222)", value: "+222" },
  { label: "美国 (+1)", value: "+1" },
  { label: "蒙古 (+976)", value: "+976" },
  { label: "孟加拉 (+880)", value: "+880" },
  { label: "秘鲁 (+51)", value: "+51" },
  { label: "缅甸 (+95)", value: "+95" },
  { label: "摩尔多瓦 (+373)", value: "+373" },
  { label: "摩洛哥 (+212)", value: "+212" },
  { label: "摩纳哥 (+377)", value: "+377" },
  { label: "纳米比亚 (+264)", value: "+264" },
  { label: "纳鲁 (+674)", value: "+674" },
  { label: "南非 (+27)", value: "+27" },
  { label: "南极洲 (+672)", value: "+672" },
  { label: "尼泊尔 (+977)", value: "+977" },
  { label: "尼加拉瓜 (+505)", value: "+505" },
  { label: "尼日利亚 (+234)", value: "+234" },
  { label: "挪威 (+47)", value: "+47" },
  { label: "葡萄牙 (+351)", value: "+351" },
  { label: "日本 (+81)", value: "+81" },
  { label: "瑞典 (+46)", value: "+46" },
  { label: "瑞士 (+41)", value: "+41" },
  { label: "萨尔瓦多 (+503)", value: "+503" },
  { label: "萨摩亚 (+685)", value: "+685" },
  { label: "塞尔维亚 (+381)", value: "+381" },
  { label: "塞拉利昂 (+232)", value: "+232" },
  { label: "塞内加尔 (+221)", value: "+221" },
  { label: "塞浦路斯 (+357)", value: "+357" },
  { label: "塞舌尔 (+248)", value: "+248" },
  { label: "沙特阿拉伯 (+966)", value: "+966" },
  { label: "圣多美和普林西比 (+239)", value: "+239" },
  { label: "圣基茨和尼维斯 (+1-869)", value: "+1-869" },
  { label: "圣卢西亚 (+1-758)", value: "+1-758" },
  { label: "圣文森特和格林纳丁斯 (+1-784)", value: "+1-784" },
  { label: "斯里兰卡 (+94)", value: "+94" },
  { label: "斯洛文尼亚 (+386)", value: "+386" },
  { label: "苏丹 (+249)", value: "+249" },
  { label: "苏里南 (+597)", value: "+597" },
  { label: "索马里 (+252)", value: "+252" },
  { label: "塔吉克斯坦 (+992)", value: "+992" },
  { label: "汤加 (+676)", value: "+676" },
  { label: "特立尼达和多巴哥 (+1-868)", value: "+1-868" },
  { label: "突尼斯 (+216)", value: "+216" },
  { label: "图瓦卢 (+688)", value: "+688" },
  { label: "土耳其 (+90)", value: "+90" },
  { label: "乌克兰 (+380)", value: "+380" },
  { label: "乌拉圭 (+598)", value: "+598" },
  { label: "委内瑞拉 (+58)", value: "+58" },
  { label: "文莱 (+673)", value: "+673" },
  { label: "乌兹别克斯坦 (+998)", value: "+998" },
  { label: "西班牙 (+34)", value: "+34" },
  { label: "希腊 (+30)", value: "+30" },
  { label: "新加坡 (+65)", value: "+65" },
  { label: "新西兰 (+64)", value: "+64" },
  { label: "匈牙利 (+36)", value: "+36" },
  { label: "叙利亚 (+963)", value: "+963" },
  { label: "牙买加 (+1-876)", value: "+1-876" },
  { label: "亚美尼亚 (+374)", value: "+374" },
  { label: "也门 (+967)", value: "+967" },
  { label: "伊拉克 (+964)", value: "+964" },
  { label: "伊朗 (+98)", value: "+98" },
  { label: "以色列 (+972)", value: "+972" },
  { label: "意大利 (+39)", value: "+39" },
  { label: "印度 (+91)", value: "+91" },
  { label: "印度尼西亚 (+62)", value: "+62" },
  { label: "英国 (+44)", value: "+44" },
  { label: "约旦 (+962)", value: "+962" },
  { label: "越南 (+84)", value: "+84" },
  { label: "赞比亚 (+260)", value: "+260" },
  { label: "乍得 (+235)", value: "+235" },
  { label: "智利 (+56)", value: "+56" },
  { label: "中非共和国 (+236)", value: "+236" }
];
const countryOptionsEn =[
  { label: "East Timor (+670)", value: "+670" },
  { label: "China (+86)", value: "+86" },
  { label: "Denmark (+45)", value: "+45" },
  { label: "Ukraine (+380)", value: "+380" },
  { label: "Uruguay (+598)", value: "+598" },
  { label: "Yemen (+967)", value: "+967" },
  { label: "Israel (+972)", value: "+972" },
  { label: "Iraq (+964)", value: "+964" },
  { label: "Iran (+98)", value: "+98" },
  { label: "Belize (+501)", value: "+501" },
  { label: "Cape Verde (+238)", value: "+238" },
  { label: "Russia (+7)", value: "+7" },
  { label: "Bulgaria (+359)", value: "+359" },
  { label: "Croatia (+385)", value: "+385" },
  { label: "Gambia (+220)", value: "+220" },
  { label: "Iceland (+354)", value: "+354" },
  { label: "Guinea (+224)", value: "+224" },
  { label: "Guinea-Bissau (+245)", value: "+245" },
  { label: "Congo (Brazzaville) (+242)", value: "+242" },
  { label: "Congo (Kinshasa) (+243)", value: "+243" },
  { label: "Libya (+218)", value: "+218" },
  { label: "Liberia (+231)", value: "+231" },
  { label: "Canada (+1)", value: "+1" },
  { label: "Ghana (+233)", value: "+233" },
  { label: "Gabon (+241)", value: "+241" },
  { label: "Antarctica (+672)", value: "+672" },
  { label: "South Africa (+27)", value: "+27" },
  { label: "Qatar (+974)", value: "+974" },
  { label: "India (+91)", value: "+91" },
  { label: "Indonesia (+62)", value: "+62" },
  { label: "Guatemala (+502)", value: "+502" },
  { label: "Ecuador (+593)", value: "+593" },
  { label: "Eritrea (+291)", value: "+291" },
  { label: "Syria (+963)", value: "+963" },
  { label: "Cuba (+53)", value: "+53" },
  { label: "Djibouti (+253)", value: "+253" },
  { label: "Costa Rica (+506)", value: "+506" },
  { label: "Cameroon (+237)", value: "+237" },
  { label: "Tuvalu (+688)", value: "+688" },
  { label: "Turkey (+90)", value: "+90" },
  { label: "Saint Lucia (+1-758)", value: "+1-758" },
  { label: "Saint Kitts and Nevis (+1-869)", value: "+1-869" },
  { label: "Algeria (+213)", value: "+213" },
  { label: "Albania (+355)", value: "+355" },
  { label: "Afghanistan (+93)", value: "+93" },
  { label: "Argentina (+54)", value: "+54" },
  { label: "United Arab Emirates (+971)", value: "+971" },
  { label: "Oman (+968)", value: "+968" },
  { label: "Egypt (+20)", value: "+20" },
  { label: "Ethiopia (+251)", value: "+251" },
  { label: "Ireland (+353)", value: "+353" },
  { label: "Estonia (+372)", value: "+372" },
  { label: "Angola (+244)", value: "+244" },
  { label: "Antigua and Barbuda (+1-268)", value: "+1-268" },
  { label: "Austria (+43)", value: "+43" },
  { label: "Australia (+61)", value: "+61" },
  { label: "Bahamas (+1-242)", value: "+1-242" },
  { label: "Pakistan (+92)", value: "+92" },
  { label: "Barbados (+1-246)", value: "+1-246" },
  { label: "Papua New Guinea (+675)", value: "+675" },
  { label: "Panama (+507)", value: "+507" },
  { label: "Bahrain (+973)", value: "+973" },
  { label: "Palestine (+970)", value: "+970" },
  { label: "Brazil (+55)", value: "+55" },
  { label: "Belarus (+375)", value: "+375" },
  { label: "Belgium (+32)", value: "+32" },
  { label: "Poland (+48)", value: "+48" },
  { label: "Bosnia and Herzegovina (+387)", value: "+387" },
  { label: "Bolivia (+591)", value: "+591" },
  { label: "North Korea (+850)", value: "+850" },
  { label: "Germany (+49)", value: "+49" },
  { label: "Togo (+228)", value: "+228" },
  { label: "Dominican Republic (+1-809)", value: "+1-809" },
  { label: "Dominica (+1-767)", value: "+1-767" },
  { label: "France (+33)", value: "+33" },
  { label: "Finland (+358)", value: "+358" },
  { label: "Philippines (+63)", value: "+63" },
  { label: "Georgia (+995)", value: "+995" },
  { label: "Grenada (+1-473)", value: "+1-473" },
  { label: "Guyana (+592)", value: "+592" },
  { label: "Kazakhstan (+7)", value: "+7" },
  { label: "Haiti (+509)", value: "+509" },
  { label: "South Korea (+82)", value: "+82" },
  { label: "Netherlands (+31)", value: "+31" },
  { label: "Montenegro (+382)", value: "+382" },
  { label: "Honduras (+504)", value: "+504" },
  { label: "Cambodia (+855)", value: "+855" },
  { label: "Czech Republic (+420)", value: "+420" },
  { label: "Zimbabwe (+263)", value: "+263" },
  { label: "Comoros (+269)", value: "+269" },
  { label: "Ivory Coast (+225)", value: "+225" },
  { label: "Kuwait (+965)", value: "+965" },
  { label: "Latvia (+371)", value: "+371" },
  { label: "Lebanon (+961)", value: "+961" },
  { label: "Lithuania (+370)", value: "+370" },
  { label: "Liechtenstein (+423)", value: "+423" },
  { label: "Luxembourg (+352)", value: "+352" },
  { label: "Rwanda (+250)", value: "+250" },
  { label: "Romania (+40)", value: "+40" },
  { label: "Madagascar (+261)", value: "+261" },
  { label: "Maldives (+960)", value: "+960" },
  { label: "Malta (+356)", value: "+356" },
  { label: "Malawi (+265)", value: "+265" },
  { label: "Malaysia (+60)", value: "+60" },
  { label: "Mali (+223)", value: "+223" },
  { label: "Marshall Islands (+692)", value: "+692" },
  { label: "Mauritius (+230)", value: "+230" },
  { label: "Mauritania (+222)", value: "+222" },
  { label: "United States (+1)", value: "+1" },
  { label: "Mongolia (+976)", value: "+976" },
  { label: "Bangladesh (+880)", value: "+880" },
  { label: "Peru (+51)", value: "+51" },
  { label: "Myanmar (+95)", value: "+95" },
  { label: "Moldova (+373)", value: "+373" },
  { label: "Morocco (+212)", value: "+212" },
  { label: "Monaco (+377)", value: "+377" },
  { label: "Namibia (+264)", value: "+264" },
  { label: "Nauru (+674)", value: "+674" },
  { label: "Nepal (+977)", value: "+977" },
  { label: "Nicaragua (+505)", value: "+505" },
  { label: "Nigeria (+234)", value: "+234" },
  { label: "Norway (+47)", value: "+47" },
  { label: "Portugal (+351)", value: "+351" },
  { label: "Japan (+81)", value: "+81" },
  { label: "Sweden (+46)", value: "+46" },
  { label: "Switzerland (+41)", value: "+41" },
  { label: "El Salvador (+503)", value: "+503" },
  { label: "Samoa (+685)", value: "+685" },
  { label: "Serbia (+381)", value: "+381" },
  { label: "Sierra Leone (+232)", value: "+232" },
  { label: "Senegal (+221)", value: "+221" },
  { label: "Cyprus (+357)", value: "+357" },
  { label: "Seychelles (+248)", value: "+248" },
  { label: "Saudi Arabia (+966)", value: "+966" },
  { label: "Sao Tome and Principe (+239)", value: "+239" },
  { label: "Saint Vincent and the Grenadines (+1-784)", value: "+1-784" },
  { label: "Sri Lanka (+94)", value: "+94" },
  { label: "Slovenia (+386)", value: "+386" },
  { label: "Sudan (+249)", value: "+249" },
  { label: "Suriname (+597)", value: "+597" },
  { label: "Somalia (+252)", value: "+252" },
  { label: "Tajikistan (+992)", value: "+992" },
  { label: "Tonga (+676)", value: "+676" },
  { label: "Trinidad and Tobago (+1-868)", value: "+1-868" },
  { label: "Tunisia (+216)", value: "+216" },
  { label: "Venezuela (+58)", value: "+58" },
  { label: "Brunei (+673)", value: "+673" },
  { label: "Uzbekistan (+998)", value: "+998" },
  { label: "Spain (+34)", value: "+34" },
  { label: "Greece (+30)", value: "+30" },
  { label: "Singapore (+65)", value: "+65" },
  { label: "New Zealand (+64)", value: "+64" },
  { label: "Hungary (+36)", value: "+36" },
  { label: "Jamaica (+1-876)", value: "+1-876" },
  { label: "Armenia (+374)", value: "+374" },
  { label: "Italy (+39)", value: "+39" },
  { label: "United Kingdom (+44)", value: "+44" },
  { label: "Jordan (+962)", value: "+962" },
  { label: "Vietnam (+84)", value: "+84" },
  { label: "Zambia (+260)", value: "+260" },
  { label: "Chad (+235)", value: "+235" },
  { label: "Chile (+56)", value: "+56" },
  { label: "Central African Republic (+236)", value: "+236" }
];

];
watch(locale, () => {
  console.log("语言已切换!!!!!!!!!!!!!!!!");
  if(locale === chinese){
    countryOptions = countryOptionsZh;
  }else{
    countryOptions = countryOptionsEn;
  }
});
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
