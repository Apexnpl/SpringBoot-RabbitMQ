package com.xyb.my_spring_boot_productor.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Daily
 * @version 1.0
 */
// 生产者只需要声明交换机，因为生产者发消息，只需要往交换机发送消息即可
@Configuration
public class MyRabbitConfig {
    private  static  String Exchange_Name = "my_boot_fanout_exchange";
    /*
     *  声明交换机
     * */
    @Bean
    public FanoutExchange exchange(){
        return new FanoutExchange(Exchange_Name, true,false);
    }

}
