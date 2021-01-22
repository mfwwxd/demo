package uyun.smc.common.enums;

public enum ContentTypeEnum {

    JSON("JSON","application/json; charset=utf-8"),  //json数据格式
    FORM("FORM", "application/x-www-form-urlencoded"),//form数据格式
    STREAM("STREAM", "application/octet-stream"),  //二进制流数据（如常见的文件下载）

    TEXT_HTML("TEXT_HTML", "text/html; charset=utf-8"),//HTML格式
    TEXT_PLAIN("TEXT_PLAIN", "text/plain; charset=utf-8"), //纯文本格式
    TEXT_XML("TEXT_XML", "text/xml; charset=utf-8"), //XML格式

    IMAGE_PNG("IMAGE_PNG", "image/png"),
    IMAGE_JPEG("IMAGE_JPEG", "image/jpeg"),
    IMAGE_GIF("IMAGE_GIF", "image/gif"),

    MULTIPART("MULTIPART", "multipart/form-data"); //需要在表单中进行文件上传时，就需要使用该格式

    private String key;
    private String value;

    ContentTypeEnum(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
