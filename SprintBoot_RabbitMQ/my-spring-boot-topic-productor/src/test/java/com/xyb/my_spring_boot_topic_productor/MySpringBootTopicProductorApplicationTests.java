package com.xyb.my_spring_boot_topic_productor;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MySpringBootTopicProductorApplicationTests {
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Test
	public void sendMessage(){
		rabbitTemplate.convertAndSend("my_boot_topic_exchange","product.topicxxx","hello world");
	}

}
