package com.xyb.my_spring_boot_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MySpringBootConsumerApplication {

	public static void main(String[] args) {
		System.out.println("hello world");
		SpringApplication.run(MySpringBootConsumerApplication.class, args);
	}

}
