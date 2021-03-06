package com.xu.flashsale.service;

import com.xu.flashsale.service.model.PromoModel;

public interface PromoService {
    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);

    void publishPromo(Integer promoId);

    String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId);
}
