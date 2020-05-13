package com.xu.flashsale.controller;

import com.xu.flashsale.error.BusinessException;
import com.xu.flashsale.error.EmBusinessError;
import com.xu.flashsale.response.CommonReturnType;
import com.xu.flashsale.service.OrderService;
import com.xu.flashsale.service.model.OrderModel;
import com.xu.flashsale.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.xu.flashsale.controller.BaseComtroller.CONTENT_TYPE_FORMED;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseComtroller {
    @Autowired
    private OrderService orderService;

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId,
                                        HttpServletRequest request) throws BusinessException {

        Boolean isLogin = (Boolean) request.getSession().getAttribute("IS_LOGIN");
        if(isLogin == null || !isLogin.booleanValue()){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }

        //获取用户的登陆信息
        UserModel userModel = (UserModel)request.getSession().getAttribute("LOGIN_USER");

        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);

        return CommonReturnType.create(null);
    }
}
