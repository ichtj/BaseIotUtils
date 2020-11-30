package com.face_chtj.base_iotutils.entity;

public class FileEntity {
    private String name;
    private String length;
    private String path;
    private String lastModified;//上一次修改日期
    private boolean isDirectory;//是否是文件

    public FileEntity(String name, String length, String path, String lastModified, boolean isDirectory) {
        this.name = name;
        this.length = length;
        this.path = path;
        this.lastModified = lastModified;
        this.isDirectory = isDirectory;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
