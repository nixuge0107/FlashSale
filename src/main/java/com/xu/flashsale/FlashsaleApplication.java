package com.xu.flashsale;

import com.xu.flashsale.dao.UserDOMapper;
import com.xu.flashsale.dataobject.UserDO;
import org.apache.ibatis.annotations.ResultMap;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.xu.flashsale"})
@MapperScan("com.xu.flashsale.dao")
@RestController
public class FlashsaleApplication {

    @Autowired
    private UserDOMapper userDOMapper;

    @RequestMapping("/")
    public String hello(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        return userDO.getName();
    }

    public static void main(String[] args) {
        SpringApplication.run(FlashsaleApplication.class, args);
    }

}
