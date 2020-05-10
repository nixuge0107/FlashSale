package com.xu.flashsale.controller;

import com.xu.flashsale.controller.viewobject.UserVO;
import com.xu.flashsale.error.BusinessException;
import com.xu.flashsale.error.CommonError;
import com.xu.flashsale.error.EmBusinessError;
import com.xu.flashsale.response.CommonReturnType;
import com.xu.flashsale.service.UserService;
import com.xu.flashsale.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController extends BaseComtroller{
    @Autowired
    private UserService userService;

    @RequestMapping("/get")
    public CommonReturnType getUser(@RequestParam(name = "id")Integer id) throws BusinessException {
        UserModel userModel = userService.getUserById(id);
        UserVO userVO = convertFromModel(userModel);

        throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        //return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
