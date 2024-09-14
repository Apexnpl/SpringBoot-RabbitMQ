package com.xyb.my_spring_boot_topic_consumer.config;

import org.springframework.amqp.core.*;
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
    private  static  String Queue_Name = "my_boot_topic_queue1";
    /*
     *  声明交换机
     * */
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(Exchange_Name, true,false);
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
     *  需要带上routingkey
     * */
    @Bean
    public Binding queueBinding(Queue queue, TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with("product.*");
    }
}
