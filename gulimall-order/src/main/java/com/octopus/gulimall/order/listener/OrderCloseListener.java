package com.octopus.gulimall.order.listener;

import com.octopus.gulimall.order.entity.OrderEntity;
import com.octopus.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author octopus
 * @date 2023/4/9 19:18
 */
@RabbitListener(queues = "order.release.order.queue")
@Service
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
