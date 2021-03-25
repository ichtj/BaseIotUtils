package com.chtj.base_framework.entity;

public class UsbDeviceInfo {
    private String name;//名称
    private String productId;
    private String manufacturerName;//制造商

    public UsbDeviceInfo(String name, String productId, String manufacturerName) {
        this.name = name;
        this.productId = productId;
        this.manufacturerName = manufacturerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }
}
