package com.ym.base.rxhttp.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zhpan on 2018/1/25.
 */
public class SslContextFactory {
  private static final String CLIENT_AGREEMENT = "TLS";
  //使用协议 //TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
  // private static final String CLIENT_AGREEMENT      = "SSL";//使用协议
  private static final String CLIENT_TRUST_MANAGER = "X.509";
  private static final String CLIENT_TRUST_KEYSTORE = "BKS";
  private static final String CLIENT_TRUST_KEY = "PKCS12";
  private static final String CLIENT_TRUST_PROVIDER = "BC";
  //不推荐使用，Android7.0之后逐步移除了第三方provider，其中包括了“BC”
  public static String TRUST_CA_PWD = "Huawei@123";
  public static String SELF_CERT_PWD = "IoM@1234";

  public static SSLSocketFactory getSSLSocketFactory(TrustManager[] mTrustManagers) {
    try {
      //TLS1.0与SSL3.0基本上没有太大的差别，可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
      SSLContext sslContext =
          SSLContext.getInstance(CLIENT_AGREEMENT);      //CLIENT_AGREEMENT = "TLC"
      sslContext.init(null, mTrustManagers, new SecureRandom());
      return sslContext.getSocketFactory();
    } catch (NoSuchAlgorithmException mE) {
      mE.printStackTrace();
    } catch (KeyManagementException mE) {
      mE.printStackTrace();
    }
    return null;
  }

  /** 这里我们实现一个不校验https，也就是忽略https证书的 */
  public static TrustManager[] getSSLTrustManager() {
    //第二种生成TrustManager[]的方式，自己实现X509TrustManager，来生成一个CustomTrustManager类
    // TrustManager[] mTrustManagers = new TrustManager[]{new CustomTrustManager()};
    return new TrustManager[] {
        new X509TrustManager() {
          @SuppressLint("TrustAllX509TrustManager")
          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType) {
          }

          @SuppressLint("TrustAllX509TrustManager")
          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType) {
          }

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] x509Certificates = new X509Certificate[0];
            return x509Certificates;
          }
        }
    };
  }

  /** 单项认证 (给出证书本地地址) */
  public static TrustManager[] getSSLTrustManager(Context context, int[] certificates) {
    if (context == null) {
      throw new NullPointerException("context == null");
    }
    try {
      //CertificateFactory用来生成证书
      CertificateFactory certificateFactory =
          CertificateFactory.getInstance(CLIENT_TRUST_MANAGER/*无需指定第二个参数*/);
      //Create a KeyStore containing our trusted CAs     创建一个密钥存储库包含我们的受信任的ca;
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      // KeyStore keyStore = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
      keyStore.load(null);
      for (int i = 0; i < certificates.length; i++) {
        //读取本地证书
        InputStream certificate = context.getResources().openRawResource(certificates[i]);
        keyStore.setCertificateEntry(String.valueOf(i),
            certificateFactory.generateCertificate(certificate));
        try {
          certificate.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);
      return trustManagerFactory.getTrustManagers();
    } catch (KeyStoreException mE) {
      mE.printStackTrace();
    } catch (NoSuchAlgorithmException mE) {
      mE.printStackTrace();
    } catch (CertificateException mE) {
      mE.printStackTrace();
    } catch (IOException mE) {
      mE.printStackTrace();
    }
    return null;
  }

  /** 单项认证 (给出证书读取流) */
  public static TrustManager[] getSSLTrustManager(InputStream... certificates) {
    try {
      CertificateFactory certificateFactory =
          CertificateFactory.getInstance(CLIENT_TRUST_MANAGER/*无需指定第二个参数*/);
      KeyStore keyStore = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
      keyStore.load(null);
      int index = 0;
      for (InputStream certificate : certificates) {
        keyStore.setCertificateEntry(String.valueOf(index),
            certificateFactory.generateCertificate(certificate));
        try {
          if (certificate != null) {
            certificate.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      //第一种生成TrustManager[]的方式，利用 Android 系统提供的 TrustManagerFactory来获取
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);
      return trustManagerFactory.getTrustManagers();
    } catch (KeyStoreException mE) {
      mE.printStackTrace();
    } catch (NoSuchAlgorithmException mE) {
      mE.printStackTrace();
    } catch (CertificateException mE) {
      mE.printStackTrace();
    } catch (IOException mE) {
      mE.printStackTrace();
    }
    return null;
  }

  // /**
  //  * 双向认证
  //  *
  //  * @return SSLSocketFactory
  //  */
  // public static SSLSocketFactory getSSLSocketFactoryForTwoWay() {
  //     try {
  //         InputStream certificate = MyApplication.getContext().getResources().openRawResource(R.raw.capk);
  //         //  CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
  //         KeyStore keyStore = KeyStore.getInstance(CLIENT_TRUST_KEY);
  //         keyStore.load(certificate, SELF_CERT_PWD.toCharArray());
  //         KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
  //         kmf.init(keyStore, SELF_CERT_PWD.toCharArray());
  //
  //         try {
  //             if (certificate != null)
  //                 certificate.close();
  //         } catch (IOException e) {
  //             e.printStackTrace();
  //         }
  //
  //         //初始化keystore
  //         KeyStore clientKeyStore = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
  //         clientKeyStore.load(MyApplication.getContext().getResources().openRawResource(R.raw.srca), TRUST_CA_PWD.toCharArray());
  //
  //         SSLContext sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
  //         TrustManagerFactory trustManagerFactory = TrustManagerFactory.
  //                 getInstance(TrustManagerFactory.getDefaultAlgorithm());
  //
  //         trustManagerFactory.init(clientKeyStore);
  //
  //         KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
  //         keyManagerFactory.init(clientKeyStore, SELF_CERT_PWD.toCharArray());
  //
  //         sslContext.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
  //         return sslContext.getSocketFactory();
  //     } catch (Exception e) {
  //         e.printStackTrace();
  //     }
  //     return null;
  // }

  /***/
  public static class SafeHostnameVerifier implements HostnameVerifier {
    ///url1，url2是你需要信任的服务器地址，例如上方new Retrofit.Builder().baseUrl(url)中
    // url="https://test2-mytest.com:8888/mytest/",url1相对应就是test2-mytest.com，验证时会自动去掉https。
    public static String urls[] = {
        "jar.bzx.net", "ss0.bdstatic.com", "ss2.bdstatic.com", "ss3.bdstatic.com", "www.bzx.net"
    };

    @Override
    public boolean verify(String hostname, SSLSession session) {
      // boolean verifier = false;
      // for (String host : urls) {
      //     // 校验hostname是否正确，如果正确则建立连接
      //     if (host.equalsIgnoreCase(hostname)) {
      //         verifier = true;
      //     }
      // }
      // return verifier;
      return true;
    }
  }
}