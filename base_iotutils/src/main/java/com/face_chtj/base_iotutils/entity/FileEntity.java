package com.face_chtj.base_iotutils.entity;

public class FileEntity {
    private String name;
    private String length;
    private String path;
    private boolean isDirectory;//是否是文件

    public FileEntity(String name, String length, String path, boolean isDirectory) {
        this.name = name;
        this.length = length;
        this.path = path;
        this.isDirectory = isDirectory;
    }
    public boolean getIsDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
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
