package com.chtj.base_framework.entity;

public class EhciInfo {
    private String product;
    private String manufacturer;
    private String path;

    public EhciInfo() {
    }

    public EhciInfo(String product, String manufacturer, String path) {
        this.product = product;
        this.manufacturer = manufacturer;
        this.path = path;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
