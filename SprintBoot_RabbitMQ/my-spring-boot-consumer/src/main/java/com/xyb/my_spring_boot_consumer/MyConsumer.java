package com.xyb.my_spring_boot_consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Daily
 * @version 1.0
 */
@Component
public class MyConsumer {

    /*
    * 监听队列
    * 当队列中有消息，则监听器工作，处理接收到的消息
    * */
    @RabbitListener(queues = "my_boot_fanout_queue1")
    public void process(Message message) {
        byte[] body = message.getBody();
        System.out.println("接收到的消息" + new String(body));
    }
}
