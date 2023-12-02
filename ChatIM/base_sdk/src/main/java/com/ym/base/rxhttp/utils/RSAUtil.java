package com.ym.base.rxhttp.utils;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * RSA 算法 是非对称加密算法
 * RSA 算法是最流行的公钥密码算法，使用长度可以变化的密钥。RSA 是第一个既能用于数据加密也能用于数字签名的算法。
 * <p>
 * RSA 工具类。提供加密，解密，生成密钥对等方法。
 * RSA 加密原理概述
 * RSA 的安全性依赖于大数的分解，公钥和私钥都是两个大素数（大于 100 的十进制位）的函数。
 * 据猜测，从一个密钥和密文推断出明文的难度等同于分解两个大素数的积
 * ===================================================================
 * （该算法的安全性未得到理论的证明）
 * ===================================================================
 * 密钥的产生：
 * 1. 选择两个大素数 p,q , 计算 n=p*q;
 * 2. 随机选择加密密钥 e , 要求 e 和 (p-1)*(q-1) 互质
 * 3. 利用 Euclid 算法计算解密密钥 d , 使其满足 e*d = 1(mod(p-1)*(q-1)) (其中 n,d 也要互质)
 * 4: 至此得出公钥为 (n,e) 私钥为 (n,d)
 * ===================================================================
 * 加解密方法：
 * 1. 首先将要加密的信息 m(二进制表示) 分成等长的数据块 m1,m2,...,mi 块长 s(尽可能大) , 其中 2^s<n
 * 2: 对应的密文是： ci = mi^e(mod n)
 * 3: 解密时作如下计算： mi = ci^d(mod n)
 * ===================================================================
 * RSA 速度
 * 由于进行的都是大数计算，使得 RSA 最快的情况也比 DES 慢上 100 倍，无论 是软件还是硬件实现。
 * 速度一直是 RSA 的缺陷。一般来说只用于少量数据 加密。
 * <p>
 * BASE64 转换说明: demo 中是使用的 android 自带的, 如果是 java 后台把对应的替换成 org.apachesBASE64，这样就能兼容 iOS 和 android 等平台
 * 秘钥长度配置: 修改 DEFAULT_KEY_SIZE 的长度即可
 */
public class RSAUtil {
    //<editor-fold defaultstate="collapsed" desc="单例实现">
    private RSAUtil() {
        updataPublicKey(mAppPublicKeyStr);
    }

    private static class SingleTonHolder {
        private static final RSAUtil INSTANCE = new RSAUtil();
    }

    public static RSAUtil getInstance() {
        return SingleTonHolder.INSTANCE;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="常量区">
    public static final Charset UTF_8 = java.nio.charset.StandardCharsets.UTF_8;     // 字符串与字节数组的编码方式
    public static final String RSA_ALGORITHM = "RSA";// 非对称加密密钥算法
    public static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";// 加密填充方式
    //请求参数
    public static final String PARAMS_KEY = "inputParamJson";
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="服务端生成公私钥后前端保存下来进行加解密">
    /**
     * 公钥
     */
    public String mAppPublicKeyStr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6amslTkuX0LsXJd8KVkWp1Hdp" +
            "pmqrynpS4kykQquQHyEmzunIMJxqdZgul9Fn/VCoj/p9+uesD50QXB4eQCl/sAXM" +
            "/kFq2fSrVdr7ZgyPIL/pFAhimEmEv0Adg1fasZ7kWbf6OTIitO1BJ0FVDdtJ+3dP" +
            "4BZMNJ6zDW3EQiLg/QIDAQAB";

    /**
     * 私钥
     */
    public String mAppPrivateKeyStr = "";
    /**
     * 当前使用的秘钥在初始化的时候使用了多大的初始化长度
     */
    private int currentKeyInitSize = -1;
    /**
     * RSA 当前秘钥支持加解密的最大字节数
     */
    private int currentKeyEncryptDecryptBlockSize = -1;
    /**
     * RSA 最大加密明文大小,//实际加密明文字节长度必须比最大加密字节数少11位
     */
    private int currentKeyEncryptStrSize = -1;

    public void updataPublicKey(String publicKey) {
        try {
            mAppPublicKeyStr = publicKey;
            initUseSize(getPublicKey(publicKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void updataPrivateKey(String privateKey) {
        try {
            mAppPrivateKeyStr = privateKey;
            initUseSize(getPrivateKey(privateKey));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据秘钥推算,初始化秘钥的长度,和所能支持的加解密字节长度,和加密明文的长度
     */
    private void initUseSize(RSAKey mRSAKey) {
        currentKeyInitSize = mRSAKey.getModulus().bitLength();
        currentKeyEncryptDecryptBlockSize = currentKeyInitSize / 8;
        currentKeyEncryptStrSize = currentKeyEncryptDecryptBlockSize - 11;
    }

    /**
     * 得到私钥对象
     *
     * @param privateKey 密钥字符串（经过base64编码）
     */
    public RSAPrivateKey getPrivateKey(String privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (TextUtils.isEmpty(privateKey)) {
            return null;
        }
        byte[] mDecodePrivateKey = Base64.decode(privateKey.getBytes(UTF_8), Base64.NO_WRAP);
        return getPrivateKey(mDecodePrivateKey);
    }

    private RSAPrivateKey getPrivateKey(byte[] mDecodePrivateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过PKCS#8编码的Key指令获得私钥对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(mDecodePrivateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * 得到公钥对象
     *
     * @param publicKey 密钥字符串（经过base64编码）
     */
    private RSAPublicKey getPublicKey(String publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (TextUtils.isEmpty(publicKey)) {
            return null;
        }
        byte[] mDecodePublicKey = Base64.decode(publicKey.getBytes(UTF_8), Base64.NO_WRAP);
        return getPublicKey(mDecodePublicKey);
    }

    private RSAPublicKey getPublicKey(byte[] mDecodePublicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        //通过X509编码的Key指令获得公钥对象
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(mDecodePublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
    }

    /**
     * RSA对字符串进行分段处理
     *
     * @return 分段处理之后的字符串
     */
    private byte[] rsaSplitCodec(Cipher cipher, int opMode, byte[] data) {
        int maxBlock;
        if (opMode == Cipher.DECRYPT_MODE) {
            maxBlock = currentKeyEncryptDecryptBlockSize;
        } else {
            maxBlock = currentKeyEncryptStrSize;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int inputLen = data.length;
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        try {
            while (inputLen > offSet) {
                if (inputLen - offSet > maxBlock) {
                    cache = cipher.doFinal(data, offSet, maxBlock);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * maxBlock;
            }
        } catch (Exception e) {
            if (opMode == Cipher.DECRYPT_MODE) {
                Log.d("RSA加解密", "解密位置为[" + offSet + "]的数据时发生异常");
            } else {
                Log.d("RSA加解密", "加密位置为[" + offSet + "]的数据时发生异常");
            }
            e.printStackTrace();
        }
        byte[] decryptedData = out.toByteArray();
        try {
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedData;
    }

    /**
     * 用公钥对字符串进行分段加密
     */
    public String encryptByPublicKey(String data) {
        return encryptByPublicKey(data.getBytes(UTF_8))/*.replaceAll("\r|\n","")*/;
    }

    /**
     * 用公钥对字节数据进行分段加密
     */
    public String encryptByPublicKey(byte[] data) {
        try {
            RSAPublicKey publicKey = getPublicKey(mAppPublicKeyStr);
            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data), Base64.NO_WRAP);
        } catch (Exception e) {
            String sourseData = new String(data, UTF_8);
            Log.d("RSA加解密", "公钥加密[" + sourseData + "]的字节数据时发生异常");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用私钥对字符串进行分段解密
     */
    public String decodeByPrivateKey(String data) {
        return decodeByPrivateKey(Base64.decode(data.getBytes(UTF_8), Base64.NO_WRAP));
    }

    /**
     * 用私钥对字节数据进行分段解密
     */
    public String decodeByPrivateKey(byte[] data) {
        try {
            RSAPrivateKey privateKey = getPrivateKey(mAppPrivateKeyStr);
            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, data), UTF_8);
        } catch (Exception e) {
            String sourseData = new String(data, UTF_8);
            Log.d("RSA加解密", "私钥解密[" + sourseData + "]的字节数据时发生异常");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用私钥对字符串进行分段加密
     */
    public String encryptByPrivateKey(String data) {
        return encryptByPrivateKey(data.getBytes(UTF_8))/*.replaceAll("\r|\n","")*/;
    }

    /**
     * 用私钥对字节数据进行分段加密
     */
    public String encryptByPrivateKey(byte[] data) {
        try {
            RSAPrivateKey privateKey = getPrivateKey(mAppPrivateKeyStr);
            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return Base64.encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data), Base64.NO_WRAP);
        } catch (Exception e) {
            String sourseData = new String(data, UTF_8);
            Log.d("RSA加解密", "公钥加密[" + sourseData + "]的字节数据时发生异常");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用公钥对字符串进行分段解密
     */
    public String decodeByPublicKey(String data) {
        return decodeByPublicKey(Base64.decode(data.getBytes(UTF_8), Base64.NO_WRAP));
    }

    /**
     * 用公钥对字节数据进行分段解密
     */
    public String decodeByPublicKey(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            RSAPublicKey publicKey = getPublicKey(mAppPublicKeyStr);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, data), UTF_8);
        } catch (Exception e) {
            String sourseData = new String(data, UTF_8);
            Log.d("RSA加解密", "私钥解密[" + sourseData + "]的字节数据时发生异常");
            e.printStackTrace();
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Rsa算法后的数据进行签名和校验">
    //定义签名算法
    private final static String KEY_RSA_SIGNATURE = "MD5withRSA";

    /**
     * 用私钥对加密数据进行签名
     */
    public String sign(String encryptedStr, String privateKey) {
        String sign = "";
        try {
            // 用私钥对信息生成数字签名
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
            signature.initSign(getPrivateKey(privateKey));
            signature.update(encryptedStr.getBytes(UTF_8));
            sign = Base64.encodeToString(signature.sign(), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    /**
     * 公钥校验数字签名
     *
     * @return 校验成功返回true，失败返回false
     */
    public boolean verify(String encryptedStr, String publicKey, String sign) {
        boolean flag = false;
        try {
            // 用公钥验证数字签名
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
            signature.initVerify(getPublicKey(publicKey));
            signature.update(encryptedStr.getBytes());
            flag = signature.verify(Base64.decode(sign, Base64.NO_WRAP));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="自己生成公钥私钥进行处理">
    private static final int DEFAULT_KEY_SIZE = 1024;// 秘钥初始化长度范围：512～2048,一般 1024
    /**
     * RSA 当前秘钥支持加解密的最大字节数
     */
    private static final int MAX_ENCRYPT_DECRYPT_BLOCK = (DEFAULT_KEY_SIZE / 8);
    /**
     * RSA 最大加密明文大小
     */
    private static final int MAX_ENCRYPT_STRING = MAX_ENCRYPT_DECRYPT_BLOCK - 11; //加密明文长度必须比加密字节数少11位

    /**
     * 运行时生成的私钥
     */
    private static String mPrivateKeyString;
    /**
     * 运行时生成的公钥
     */
    private static String mPublicKeyString;
    /**
     * 公钥对象
     */
    private static PublicKey mPublic;
    /**
     * 私钥对象
     */
    private static PrivateKey mPrivate;

    //RSA 的初始化，获得私钥和密钥
    public static void rsaInit() {
        try {
            //随机生成 RSA 密钥对
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);//RAS 密钥生成器
            //密钥长度，范围：512～2048,一般 1024
            kpg.initialize(DEFAULT_KEY_SIZE, new SecureRandom());//生成制定长度的密钥
            KeyPair keyPair = kpg.generateKeyPair();//生成密钥对
            mPrivate = keyPair.getPrivate();//获取私钥
            mPublic = keyPair.getPublic();//获取公钥
            //通过getEncoded方法来获取密钥的具体内容
            byte[] privateEncoded = mPrivate.getEncoded();
            byte[] publicEncoded = mPublic.getEncoded();
            //为了防止乱码，使用Base64来转换，这样显示的时候就不会有乱码了
            mPrivateKeyString = Base64.encodeToString(privateEncoded, Base64.NO_WRAP);
            mPublicKeyString = Base64.encodeToString(publicEncoded, Base64.NO_WRAP);

            Log.d("RSA加解密", "RSA私钥：" + mPrivateKeyString);
            Log.d("RSA加解密", "RSA公钥：" + mPublicKeyString);
        } catch (NoSuchAlgorithmException e) {
            Log.d("RSA加解密", "RSA秘钥初始化失败：未匹配到指定的[" + RSA_ALGORITHM + "]算法");
            e.printStackTrace();
        }
    }
    //</editor-fold>

    public static void main() {
        String str =
                "{\"userName\":\"testgagacaw\",\"password\":\"test22224\",\"inviteCode\":\"127328\",\"key\":\"3b543c33-d979-42a5-9c21-70b4034227ed\",\"code\":\"pftty\"}";

        System.out.println("加密前：" + str);
        String s1 = RSAUtil.getInstance().encryptByPublicKey(str);
        System.out.println("加密后：" + s1);
        String s2 = RSAUtil.getInstance().decodeByPrivateKey(s1);
        System.out.println("解密后：" + s2);
    }
}