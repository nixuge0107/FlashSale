package com.xu.flashsale.service.impl;

import com.xu.flashsale.dao.UserDOMapper;
import com.xu.flashsale.dao.UserPasswordDOMapper;
import com.xu.flashsale.dataobject.UserDO;
import com.xu.flashsale.dataobject.UserPasswordDO;
import com.xu.flashsale.service.UserService;
import com.xu.flashsale.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    public UserModel getUserById(int id){
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);
        return convertFromDataObject(userDO, userPasswordDO);
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        userModel.setEncrpPassword(userPasswordDO.getEncrpPassword());
        return userModel;
    }

}
