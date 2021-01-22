package uyun.smc.common.enums;

public enum AccessModeEnum {

    ROUTING("ROUTING", "0"),
    ADDRESSING("ADDRESSING","1");

    private String key;
    private String value;

    AccessModeEnum(String key, String value){
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
