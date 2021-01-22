package uyun.smc.config;

public class SdkConfig {
    /**
     * 一级缓存失效时间，单位秒，默认300s=5mins
     */
    public static long CACHE_EXPIRED_TIME = 300;

    /**
     * smc连接超时时间，单位秒，默认设置30s
     */
    public static long SMC_CONNECT_TIME_OUT = 30;
    /**
     * smc写超时时间，单位秒，默认设置30s
     */
    public static long SMC_WRITE_TIME_OUT = 30;
    /**
     * smc读超时时间，单位秒，默认设置30s
     */
    public static long SMC_READ_TIME_OUT = 30;

    /**
     * direct连接超时时间，单位秒，默认设置30s
     */
    public static long DIRECT_CONNECT_TIME_OUT = 30;
    /**
     * direct写超时时间，单位秒，默认设置30s
     */
    public static long DIRECT_WRITE_TIME_OUT = 30;
    /**
     * direct读超时时间，单位秒，默认设置30s
     */
    public static long DIRECT_READ_TIME_OUT = 30;

}
