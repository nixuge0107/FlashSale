package com.xu.flashsale.service.model;

public class UserModel {

    private Integer id;
    private String name;
    private Boolean gender;
    private Integer age;
    private String telphone;
    private String registerMode;
    private String thirdPartId;
    private String encrpPassword;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    public String getThirdPartId() {
        return thirdPartId;
    }

    public void setThirdPartId(String thirdPartId) {
        this.thirdPartId = thirdPartId;
    }

    public String getEncrpPassword() {
        return encrpPassword;
    }

    public void setEncrpPassword(String encrpPassword) {
        this.encrpPassword = encrpPassword;
    }
}
