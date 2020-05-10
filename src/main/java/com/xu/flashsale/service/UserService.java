package com.xu.flashsale.service;

import com.xu.flashsale.error.BusinessException;
import com.xu.flashsale.service.model.UserModel;

public interface UserService {
    UserModel getUserById(int id);

    void register(UserModel userModel) throws BusinessException;

    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;

}
