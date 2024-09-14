# Spring-boot+RabbitMQ

## 一、创建项目

[Spring Initializr](https://start.spring.io/)

## 1. Spring Initializr 创建项目

- 添加依赖
  - spring web
  - spring for rabbitmq

<img src="C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240914145647931.png" alt="image-20240914145647931" style="zoom:50%;" />



- IDEA打开项目，修改配置

  - 修改Spring-boot的版本

    ​	![image-20240914150635759](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240914150635759.png)

  - 修改JAVA的版本 17 => 8

    ![image-20240914150711622](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240914150711622.png)

## 2.  修改yml配置

```yaml
server:
  port:8090
spring:
  rabbitmq:
    host: 123.60.179.15
    port: 5672
    username: user01
    password: 123456
    virtual-host: testhost
```

## 3. RabbitMQ配置

### 3.1 创建config的package，然后新建RabbitMQ的配置类

![image-20240914153320339](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240914153320339.png)

### 3.2 完成RabbitMQ的配置类

```java
@Configuration
public class MyRabbitConfig {

    private  static  String Exchange_Name = "my_boot_fanout_exchange";
    private  static  String Queue_Name = "my_boot_fanout_queue1";
    /*
    *  声明交换机
    * */
    @Bean
    public FanoutExchange exchange(){
        xxx
    }

    /*
     *  声明队列
     * */
    @Bean
    public Queue queue(){
       xxx
    }
}
```

- 交换器

```java
 /*
    *  声明交换机
    param1: 交换机名称
    param2: durable
    param3: autodelete
    * */
    @Bean
    public FanoutExchange exchange(){
        return new FanoutExchange(Exchange_Name, true,false);
    }
```

- 队列

```java
/*
*  声明队列
param1: 队列名称
param2: durable 持久新
param3: exclusive 排他性 队列只能创建他的连接访问
param4：autodelete  最后一个消费者断开连接后，队列自动删除
自由组合: 
① new Queue("queue_name", true, true, true)队列是持久化的，但只能由创建它的连接访问，并且在连接关闭时自动删除。
② new Queue("queue_name", false, true, true)队列是非持久化的，并且在最后一个消费者断开连接后自动删除。
③ new Queue("queue_name", true, false, true)队列是持久化的，并且在最后一个消费者断开连接后自动删除。
⑤ new Queue("queue_name", true, false, false) 队列是持久化的，并且不会自动删除。
*  
*/ 
@Bean
public Queue queue(){
    return new Queue(Queue_Name,true,false,false);
}
```

- 绑定关系

```java
```

## 二、



​	