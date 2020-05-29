package com.xu.flashsale.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.xu.flashsale.error.BusinessException;
import com.xu.flashsale.error.EmBusinessError;
import com.xu.flashsale.mq.MqProducer;
import com.xu.flashsale.response.CommonReturnType;
import com.xu.flashsale.service.ItemService;
import com.xu.flashsale.service.OrderService;
import com.xu.flashsale.service.PromoService;
import com.xu.flashsale.service.model.OrderModel;
import com.xu.flashsale.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseComtroller {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private PromoService promoService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProducer mqProducer;

    private ExecutorService executorService;

    private RateLimiter rateLimiter;

    @PostConstruct
    private void init() {
        executorService = Executors.newFixedThreadPool(20);
        rateLimiter = RateLimiter.create(300);
    }
    //生成秒杀令牌
    @RequestMapping(value = "/generatetoken",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generatetoken(@RequestParam(name="itemId")Integer itemId,
                                          @RequestParam(name="promoId")Integer promoId,
                                          HttpServletRequest httpServletRequest) throws BusinessException {
        //根据token获取用户信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        //获取用户的登陆信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        //获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId,itemId,userModel.getId());

        if(promoToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        //返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId,
                                        @RequestParam(name="promoToken",required = false)String promoToken,
                                        HttpServletRequest request) throws BusinessException {

        //令牌桶限流
        if (!rateLimiter.tryAcquire()) {
            throw new BusinessException(EmBusinessError.RATELIMIT);
        }

        String token = request.getParameter("token");
        if(token == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        //获取用户的登陆信息
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        //校验秒杀令牌是否正确
        if(promoId != null){
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userModel.getId()+"_itemid_"+itemId);
            if(inRedisPromoToken == null){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
        }
        Future<Object> future=executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                String stockLogId = itemService.initStockLog(itemId, amount);

                if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, stockLogId)) {
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"下单失败");
                }
                return null;
            }
        });
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }

        return CommonReturnType.create(null);
    }
}
