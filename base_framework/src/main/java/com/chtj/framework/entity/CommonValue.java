package com.chtj.framework.entity;

public enum CommonValue {
    EXEU_COMPLETE(0, "执行成功"),
    EXEU_UNKNOWN_ERR(1, "未知错误"),
    KL_SERVICE_REPEAT(2, "SERVICE重复添加"),
    KL_FILE_WRITE_ERR(3, "文件写入失败"),
    KL_FILE_DEL_ERR(4, "文件删除失败"),
    KL_DATA_ISNULL(5, "保活记录为空"),
    KL_SERVICE_ACTIVATE_ERR(6, "SERVICE启动失败"),
    ETH_PARAMS_ERR(7, "ip,mask or dnsAddr is wrong"),
    ETH_SECURITY_ERR(8, "android.uid.system未添加"),
    ETH_IPCHECK_ERR(9, "ip,mask,dns,getway等地址校验失败"),
    ETH_OTHER_DEVICES(10, "目前不适配此设备"),
    CMD_READ_ONLY(11, "只读系统");

    int type;
    String remarks;

    CommonValue(int type, String remarks) {
        this.type = type;
        this.remarks = remarks;
    }

    public int getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public String toString() {
        return "CommonValue{" +
                "type=" + type +
                ", value='" + remarks + '\'' +
                '}';
    }
}
