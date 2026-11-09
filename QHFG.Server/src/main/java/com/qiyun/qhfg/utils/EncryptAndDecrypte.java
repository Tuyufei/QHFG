package com.qiyun.qhfg.utils;


import cn.dev33.satoken.util.SaResult;
import org.apache.commons.codec.binary.StringUtils;
import org.hsqldb.lib.StringUtil;


import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * @PACKAGE_NAME: com.example.fxpgJava.utils
 * @NAME: EncryptAndDecrypte
 * @USER: spike
 * @DATE: 2023/4/11 10:33
 * @PROJECT_NAME: FXPG_Java
 */

public class EncryptAndDecrypte {
    static String strType = "DESede";
    static String strCipher = "DESede/CBC/PKCS5Padding";

    final static String UTF_8 = "UTF-8";

    /**
     * 默认加密字符串。
     *
     * @param encryptString
     * @return
     */
    public static String encryptString(String encryptString, String strKey, String strIV) throws Exception {
        if (!checkStringNull(encryptString)) {
            return encryptString;
        }
        return EncryptAndDecrypte.encrypt(encryptString.getBytes(StandardCharsets.UTF_8), fromBase64String(strKey), fromBase64String(strIV));
    }

    /**
     * 默认解密字符串
     *
     * @param encryptedConnectionString
     * @return
     */
    public static String decrypteString(String encryptedConnectionString, String strKey, String strIV) throws Exception {
        if (!checkStringNull(encryptedConnectionString)) {
            return encryptedConnectionString;
        }
        return EncryptAndDecrypte.decrypt(fromBase64String(encryptedConnectionString), fromBase64String(strKey), fromBase64String(strIV));
    }

    /**
     * 使用指定的 Key 和 IV 加密 。
     *
     * @param cryptoBytes 明文
     * @param key         密钥
     * @param iv          IV
     * @return Base64编码的密文
     * @throws Exception
     */
    private static String encrypt(byte[] cryptoBytes, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(strCipher);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, strType);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] cipherTextBytes = cipher.doFinal(cryptoBytes);
        return toBase64String(cipherTextBytes);
    }

    /**
     * 使用指定的 Key 和 IV 解密 。
     *
     * @param cryptoBytes data Base64编码的密文
     * @param key         密钥
     * @param iv          IV
     * @return 明文
     * @throws Exception
     */
    private static String decrypt(byte[] cryptoBytes, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(strCipher);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, strType);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] cipherTextBytes = cipher.doFinal(cryptoBytes);
        return new String(cipherTextBytes, UTF_8);
    }


    private static String toBase64String(byte[] listByte) {
        String result = Base64.getEncoder().encodeToString(listByte);
        return result;
    }

    private static byte[] fromBase64String(String src) {
        byte[] decode = Base64.getDecoder().decode(src.getBytes(StandardCharsets.UTF_8));
        return decode;
    }

    private static boolean checkStringNull(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取geoserver.jceks存储对应用户组的密匙
     *
     * @return
     */
    public static Map<String, String> getGeoserverJCEKS(String password,String path) {
        Map<String, String> jceksMap = new HashMap<>();
        try {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            //密匙库文件 geoserver\data\security\geoserver.jceks
            keyStore.load(new FileInputStream(path+"\\data\\security\\geoserver.jceks"), password.toCharArray());
            // 获取密钥库中的所有别名
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    // 获取密钥
                    Key key = keyStore.getKey(alias, password.toCharArray());
                    // 获取证书
                    Certificate cert = keyStore.getCertificate(alias);
                    // 处理密钥和证书
                    jceksMap.put(alias, StringUtils.newStringUtf8(key.getEncoded()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jceksMap;
    }

    public static SaResult strongerPBE(String password, String key)  {
        if (password.split(":").length > 1) {
            password = password.split(":")[1];
        }
        //获取geoserver.jceks存储对应用户组的密匙
        //geoserver\data\security\masterpw.info 中获取初始化密码  tpqH)MpX
        Map<String, String> map = getGeoserverJCEKS("tpqH)MpX","D:\\project\\geoserver");
        //添加加密方式
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SaResult saResult = SaResult.data(null);
        if (!StringUtil.isEmpty(key)) {
            //选择用户组
            saResult = decrypt(password, map.get(key));
        }
        saResult.put("加密用户组", map);
        return saResult;
    }

    public static SaResult decrypt(String src, String password) {
        SaResult saResult = SaResult.data(null);
        try {
            //盐 salt
            byte[] decoded = fromBase64String(src);
            int saltStart = 0;
            int saltSize = 16;
            byte[] salt = new byte[saltSize];
            //截取加密密码转换base64后,前saltSize位 作为盐
            System.arraycopy(decoded, saltStart, salt, 0, saltSize);
            //口令与密钥
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHSHA256AND256BITAES-CBC-BC");
            Key key = factory.generateSecret(pbeKeySpec);
            PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 1000);
            Cipher cipher = Cipher.getInstance("PBEWITHSHA256AND256BITAES-CBC-BC");
            //解密
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParameterSpec);
            byte[] decrypt = new byte[decoded.length-saltSize];
            //去掉加密密码转换base64前saltSize位,然后进行解密
            System.arraycopy(decoded, saltSize, decrypt, 0, decoded.length-saltSize);
            byte[] result = cipher.doFinal(decrypt);
            saResult.put("解密后密码", new String(result, UTF_8));
            //加密
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParameterSpec);
            byte[] afterResult = cipher.doFinal(result);
            //将盐和加密密码拼接
            result = appendArrays(salt, afterResult);
            saResult.put("加密后密码", "crypt2:" + toBase64String(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saResult;
    }

    public static byte[] appendArrays(final byte[] firstArray, final byte[] secondArray) {
        final byte[] result = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, result, 0, firstArray.length);
        System.arraycopy(secondArray, 0, result, firstArray.length, secondArray.length);
        return result;
    }

}
