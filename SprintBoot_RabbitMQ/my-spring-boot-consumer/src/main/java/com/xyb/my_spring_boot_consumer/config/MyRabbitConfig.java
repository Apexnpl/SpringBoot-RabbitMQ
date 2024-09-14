package com.xyb.my_spring_boot_consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Daily
 * @version 1.0
 */
@Configuration
public class MyRabbitConfig {

    private  static  String Exchange_Name = "my_boot_fanout_exchange";
    private  static  String Queue_Name = "my_boot_fanout_queue1";
    /*
    *  声明交换机
    * */
    @Bean
    public FanoutExchange exchange(){
        return new FanoutExchange(Exchange_Name, true,false);
    }

    /*
     *  声明队列
     * */
    @Bean
    public Queue queue(){
        return new Queue(Queue_Name,true,false,false);
    }

    /*
     *  声明绑定关系
     * */
    @Bean
    public Binding queueBinding(Queue queue,FanoutExchange fanoutExchange){
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }
}
