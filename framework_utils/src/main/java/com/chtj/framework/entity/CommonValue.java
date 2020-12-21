package com.chtj.framework.entity;

public enum CommonValue {
    KL_EXEU_COMPLETE(0, "执行成功"),
    KL_SERVICE_REPEAT(1, "SERVICE重复添加"),
    KL_FILE_WRITE_ERR(2, "文件写入失败"),
    KL_FILE_DEL_ERR(3, "文件删除失败"),
    KL_DATA_ISNULL(4, "保活记录为空"),
    KL_SERVICE_ACTIVATE_ERR(5, "SERVICE启动失败");

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
