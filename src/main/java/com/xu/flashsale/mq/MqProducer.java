package com.xu.flashsale.mq;

import com.alibaba.fastjson.JSON;
import com.xu.flashsale.error.BusinessException;
import com.xu.flashsale.service.ItemService;
import com.xu.flashsale.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {

    private DefaultMQProducer producer;
    private TransactionMQProducer transactionMQProducer;

    @Autowired
    private OrderService orderService;
    @Autowired
    private ItemService itemService;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @PostConstruct
    private void init() throws MQClientException {
        producer = new DefaultMQProducer("producer_group");
        producer.setNamesrvAddr(nameAddr);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();
        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object arg) {
                Integer itemId = (Integer) ((Map)arg).get("itemId");
                Integer promoId = (Integer) ((Map)arg).get("promoId");
                Integer userId = (Integer) ((Map)arg).get("userId");
                Integer amount = (Integer) ((Map)arg).get("amount");
                String stockLogId = (String) ((Map)arg).get("stockLogId");

                try {
                    orderService.createOrder(userId, itemId, promoId, amount, stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    itemService.setStockLogStatus(stockLogId, 3);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                String jsonString = new String(messageExt.getBody());
                Map<String, Object> body = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) body.get("itemId");
                Integer amount = (Integer) body.get("amount");
                String stockLogId = (String) body.get("stockLogId");
                int stockLogStatus = itemService.getStockLogStatus(stockLogId);
                if (stockLogStatus == 2) {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                } else if (stockLogStatus == 1) {
                    return LocalTransactionState.UNKNOW;
                }

                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });
    }


    //同步库存扣减消息
    public boolean transactionAsyncReduceStock(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("stockLogId", stockLogId);

        Map<String, Object> args = new HashMap<>();
        args.put("userId", userId);
        args.put("itemId", itemId);
        args.put("promoId", promoId);
        args.put("amount", amount);
        args.put("stockLogId", stockLogId);

        Message message = new Message(topicName, "increase", JSON.toJSONString(bodyMap).getBytes(Charset.forName("UTF-8")));
        TransactionSendResult sendResult = null;
        try {
            sendResult = transactionMQProducer.sendMessageInTransaction(message,args);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE;
    }


    //同步库存扣减消息
    public boolean asyncReduceStock(Integer itemId, Integer amount) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName, "increase", JSON.toJSONString(bodyMap).getBytes(Charset.forName("UTF-8")));
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
