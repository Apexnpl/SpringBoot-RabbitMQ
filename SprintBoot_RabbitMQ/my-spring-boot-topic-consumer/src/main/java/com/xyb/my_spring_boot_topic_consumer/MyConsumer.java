package com.xyb.my_spring_boot_topic_consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * @author Daily
 * @version 1.0
 */
@Component
public class MyConsumer {

    @RabbitListener(queues = "my_boot_topic_queue1")
    public void process(Message message, Channel channel) throws IOException {
        byte[] body = message.getBody();
        System.out.println(new String(body));
        System.out.println(body.toString());
        // 手动ACK，告知brocker要签收的消息id（deliveryTag）
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
