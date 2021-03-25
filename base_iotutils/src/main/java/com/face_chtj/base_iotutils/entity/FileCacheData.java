package com.face_chtj.base_iotutils.entity;
import java.util.Map;

public class FileCacheData {
    private String iid;
    private String url;
    private String fileName;
    private String filePath;
    private String requestTag;
    private Map<String,String> spareList;//备用字段请存储在这里
    private long current;
    private long total;

    public Map<String, String> getSpareList() {
        return spareList;
    }

    public void setSpareList(Map<String, String> spareList) {
        this.spareList = spareList;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestTag() {
        return requestTag;
    }

    public void setRequestTag(String requestTag) {
        this.requestTag = requestTag;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
