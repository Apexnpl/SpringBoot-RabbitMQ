package com.xyb.my_spring_boot_productor;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MySpringBootProductorApplicationTests {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Test
	void testSendMessage(){
		rabbitTemplate.convertAndSend("my_boot_fanout_exchange","",new String("hello,a spring message"));
	}

}
