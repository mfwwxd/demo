package uyun.smc.utils;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.smc.config.SdkConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SdkUtils {

    private static final Logger logger = LoggerFactory.getLogger(SdkUtils.class);

    /**
     * 获取时间戳，精确到秒
     */
    public static String getTimestampStr(){
        return System.currentTimeMillis() / 1000 + "";
    }

    /**
     * 获取UUID
     */
    public static String getUUID(){
        return UUID.randomUUID().toString();
    }

    /**
     * Object转为String(当Object为null时，返回“null”字符串)
     */
    public static String objToString(Object obj){
        return String.valueOf(obj);
    }

    /**
     * 获取okHttpClient实例（根据url协议类型，获取client;https时，选择绕过证书策略）
     */
    public static OkHttpClient buildSmartOkHttpClient(String url, long connectTimeout, long writeTimeout, long readTimeout){

        if(url !=null && url.startsWith("https://")) {//https协议，设置忽略证书
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                X509TrustManager trustManager = new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {return null;}
                    public void checkClientTrusted(X509Certificate[] arg0,String arg1) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] arg0,String arg1) throws CertificateException {}
                };
                sslContext.init(null, new TrustManager[] { trustManager }, null);
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                return new OkHttpClient.Builder()
                        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                        .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                        .readTimeout(readTimeout, TimeUnit.SECONDS)
                        .sslSocketFactory(sslSocketFactory, trustManager)  //这个参数是关键
                        .build();
            } catch (Exception e) {//如果设置出错，则返回default实例
                logger.error("##sslContext-exception##", e);
            }
        }

        // http协议，用原生创建方式，又或者https报错，则退化为返回okhHttpClient访问实例
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
    }

}
