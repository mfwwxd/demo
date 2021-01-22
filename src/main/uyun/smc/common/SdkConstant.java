package uyun.smc.common;

public class SdkConstant {

    /**
     * 访问密钥keyName
     */
    public static final String APPKEY = "X-Smc-Appkey";
    /**
     * 随机字符串keyName，UUID
     */
    public static final String NONCE = "X-Smc-Nonce";
    /**
     * 时间戳keyName，long型，精确到秒，例如System.currentTimeMillis()/1000
     */
    public static final String TIMESTAMP = "X-Smc-Timestamp";
    /**
     * 区域编码keyName，如HZ
     */
    public static final String REGION = "X-Smc-Region";
    /**
     * 签名字符串keyName
     */
    public static final String SIGNATURE = "X-Smc-Signature";
    /**
     * 寻址模式keyName
     */
    public static final String ACCESS_MODE = "X-Smc-AccessMode";
    /**
     * 远程访问地址keyName，即跳过smc直接访问业务服务的地址
     */
    public static final String REMOTE_URL = "X-Smc-RemoteUrl";
    /**
     * 内容类型keyName
     */
    public static final String CONTENT_TYPE = "Content-Type";
    /**
     * 内容处置keyName
     */
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    /**
     * 文件类型关键字
     */
    public static final String FILE_HEADER_KEYWORD = "attachment";

}
