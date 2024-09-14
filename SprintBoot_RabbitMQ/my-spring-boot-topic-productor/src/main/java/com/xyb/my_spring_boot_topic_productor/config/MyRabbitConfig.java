package com.xyb.my_spring_boot_topic_productor.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Daily
 * @version 1.0
 */
@Configuration
public class MyRabbitConfig {
    /*
     * 声明队列、交换机、绑定关系(routing-key)
     * */
    private  static  String Exchange_Name = "my_boot_topic_exchange";

    /*
     *  声明交换机
     * */
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(Exchange_Name, true,false);
    }
}
