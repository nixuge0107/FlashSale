package com.xu.flashsale.controller;

import com.xu.flashsale.error.BusinessException;
import com.xu.flashsale.error.CommonError;
import com.xu.flashsale.error.EmBusinessError;
import com.xu.flashsale.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;

public class BaseComtroller {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public Object handleException(HttpServletRequest httpServletRequest, Exception ex) {
        HashMap<String, Object> date = new HashMap<>();
        if (ex instanceof BusinessException) {
            BusinessException businessException = (BusinessException) ex;
            date.put("errCode", businessException.getErrCode());
            date.put("errMsg", businessException.getErrMsg());
        }else {
            date.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            date.put("errMsg", EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(date,"fail");
    }
}
