package com.future.xlink.bean.common;

/**
 * 请求响应消息类型
 * */
public enum IntentType {
    INTENT_TYPE_1(1,"getProperties","获得属性"),
    INTENT_TYPE_2(2,"setProperties","设置属性"),
    INTENT_TYPE_3(3,"action","调用设备方法"),
    INTENT_TYPE_4(4,"upgrade","固件升级"),
    INTENT_TYPE_0(0,"other","其他自定义请求类型");
    int type;
    String value;
    String desicrption;

    IntentType(int type, String value,String desicrption) {
        this.type = type;
        this.value = value;
        this.desicrption=desicrption;
    }

    public String getValue() {
        return value;
    }

    public int getTye() {
        return type;
    }

    public String getDesicrption() {
        return desicrption;
    }

    public  boolean compareValue(String str){
       return value.equalsIgnoreCase(str);
    }
    @Override
    public String toString() {
        return value;
    }
}
