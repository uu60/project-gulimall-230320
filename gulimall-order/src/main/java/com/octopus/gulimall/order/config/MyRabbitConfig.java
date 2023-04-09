package com.octopus.gulimall.order.config;

import com.octopus.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author octopus
 * @date 2023/4/9 01:38
 */
@Configuration
public class MyRabbitConfig {

    @RabbitListener(queues = "order.release.order.queue")
    public void listener(OrderEntity orderEntity, Channel channel, Message message) {
        System.out.println("收到过期的订单信息：准备关闭订单" + orderEntity.getOrderSn());
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 容器中的Binding, Exchange, Queue, Exchange在不存在的情况下都会自动创建
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", "order-event-exchange");
        params.put("x-dead-letter-routing-key", "order.release.order");
        params.put("x-message-ttl", 60000);
        Queue queue = new Queue("order.delay.queue", true, false, false, params);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        Queue queue = new Queue("order.release.order.queue", true, false, false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }

    @Bean
    ApplicationRunner runner(ConnectionFactory cf) {
        return args -> cf.createConnection().close();
    }


}
