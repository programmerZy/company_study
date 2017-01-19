package com.mq.producer;

import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.mq.MqConfig;

/**
 * MQ 使用Spring发�?�普通消�?
 */
public class MQProducer4Spring {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("producer/producer.xml");
        ProducerBean producer = (ProducerBean) ctx.getBean("producer");
        System.out.println("Producer Started");
        for (int i = 0; i < 10; i++) {
            Message message = new Message(MqConfig.TOPIC, MqConfig.TAG, "mq send message test".getBytes());
            SendResult sendResult = producer.send(message);
            if (sendResult != null) {
                System.out.println(new Date() + " Send mq message success! Topic is:" + MqConfig.TOPIC + "msgId is: " + sendResult.getMessageId());
            }
        }
    }


}
