package com.ichtj.basetools.entity;

/**
 * Create on 2020/6/28
 * author chtj
 * desc
 */
public class ExcelEntity {
    private String Id;
    private String Name;
    private String Sex;
    private String Age;

    public ExcelEntity(String id, String name, String sex, String age) {
        Id = id;
        Name = name;
        Sex = sex;
        Age = age;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSex() {
        return Sex;
    }

    public void setSex(String sex) {
        Sex = sex;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }
}
