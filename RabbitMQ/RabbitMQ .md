# RabbitMQ使用教程

[RabbitMQ使用教程(超详细)-CSDN博客](https://blog.csdn.net/lzx1991610/article/details/102970854)

【知识补充】：

- MQ（Message Queue，简称MQ），本质是个队列，FIFO先进先出。

- 主要用途：不同进程Process/线程Thread之间的通信

- 为什么会产生消息队列：
  - 不同进程(Process)之间传递消息，两个进程之间耦合程度过高，改动一个进程就需要修改另一个进程，为了隔离两个进程，从两个进程间抽离出一层，所有两个进程之间传递的信息，都必须通过消息队列来传递，从而保证单独修改一个进程，不会影响到另一个。
  - 不同进程(Process)之间传递消息，为了实现标准化，将消息的格式规范化了，并且，某一个金恒接受的消息太多，一下子无法处理完，并且也有先后顺序，必须对收到的消息进行排队，因此诞生了事实上的消息队列。

- AMQP（Advanced Message Queuing Protocol）:一个提供统一消息服务的应用层标准高级消息队列协议，是应用层协议的一个开放标准。



## 一、RabbitMQ安装

- 【windows】



- 【Linux】[编程环境和软件工具安装手册.pdf](file:///E:/服务器/编程环境和软件工具安装手册.pdf)

  - 先需要erlang的支持

    ```shell
    curl -s https://packagecloud.io/install/repositories/rabbitmq/erlang/script.rpm.sh | sudo bash
    ```

    测试erl是否成功

    ```shell
    erl
    ```

    ![image-20240910154750572](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910154750572.png)

  - 安装rabbitmq

    - 安装对应的yum repo

    ```shell
    curl-s https://packagecloud.io/install/repositories/rabbitmq/rabbitmq-server/script.rpm.sh | sudo bash
    ```

    ![image-20240910154916197](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910154916197.png)

    - 正式安装rabbitmq

    ```shell
    yum install rabbitmq-server-3.8.3-1.el7.noarch
    ```

    ![image-20240910155236898](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910155236898.png)

- 设置RabbitMQ开机启动

  ```shell
  chkconfig rabbitmq-server on
  ```

- 启动RabbitMQ服务

  ```shell
  systemctl start rabbitmq-server.service
  ```

- 开启WEB可视化管理插件

  ```shell
  rabbitmq-plugins enable rabbitmq_management
  ```

  - 访问可视化管理界⾯：

    ```shell
    服务器IP:15672
    ```

    ![image-20240910155533655](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910155533655.png)

- 后台添加 用户/密码 对

  ```shell
  rabbitmqctl add_user user01 123456 # 添加用户
  rabbitmqctl set_user_tags user01 administrator # 设置用户 user01 为管理员
  ```

  登录成功后：

  ![image-20240910162230788](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910162230788.png)

## 二、RabbitMQ角色划分

### 1.用户角色

- 超级管理员(administrator)：
- 监控者(monitoring)：
- 策略制定者(monitoring)：
- 普通管理者(management)：
- 其他：

### 2. 创建Virtual Hosts

![image-20240910165524573](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910165524573.png)

#### 2.1 为用户设置权限

![image-20240910165718670](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910165718670.png)

在Admin检查是否权限已经添加

![image-20240910165835774](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910165835774.png)

#### 2.2 管理界面的功能

![image-20240910165903451](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910165903451.png)

![image-20240910170022534](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910170022534.png)

## 三、学习五种队列

项目下载地址：百度网盘 => 开发 => RabbitMQ

![image-20240910170046674](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910170046674.png)

### 1. 简单队列

- P：product消息生产者
- C：Customer消息的消费者
- 红色：消息队列

![image-20240910173725280](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910173725280.png)



引入依赖：

```xml
<dependency>
   <groupId>com.rabbitmq</groupId>
   <artifactId>amqp-client</artifactId>
   <version>3.4.1</version>
</dependency>

```

#### 1.1 获取MQ的连接

```java
package com.zpc.rabbitmq.util;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

public class ConnectionUtil {

    public static Connection getConnection() throws Exception {
        //定义连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置服务地址
        factory.setHost("123.60.179.15"); // RabbitMQ的server所在ip
        //端口
        factory.setPort(5672);  //  RabbitMQ的server所在port，15672是web可视化端口
        //设置账号信息，用户名、密码、vhost
        factory.setVirtualHost("testhost");
        factory.setUsername("user01");
        factory.setPassword("123456");
        // 通过工程获取连接
        Connection connection = factory.newConnection();
        return connection;
    }
}

```

#### 1.2 生产者发送消息到队列

```java
package com.zpc.rabbitmq.simple;

import com.zpc.rabbitmq.util.ConnectionUtil;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Send {

    private final static String QUEUE_NAME = "q_test_01";

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        // 从连接中创建通道
        Channel channel = connection.createChannel();

        // 声明（创建）队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 消息内容
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        //关闭通道和连接
        channel.close();
        connection.close();
    }
}

```

**在Queues中，点击name**

<img src="C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910190155700.png" alt="image-20240910190155700" style="zoom:50%;" />

**点击Get Message，可以看到SEND发送的内容**

<img src="C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910190255511.png" alt="image-20240910190255511" style="zoom:50%;" />



#### 1.3 消费者从队列中获取消息

**获取消息，需要指明队列名称QUEUE_NAME**

```java
package com.zpc.rabbitmq.simple;

import com.zpc.rabbitmq.util.ConnectionUtil;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

public class Recv {

    private final static String QUEUE_NAME = "q_test_01";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        // 从连接中创建通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);

        // 监听队列
        channel.basicConsume(QUEUE_NAME, true, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
        }
    }
}
```

### 2. Work模式

#### 2.1 普通Work模式

一个生产者，两个消费者：

![image-20240910195229443](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910195229443.png)

##### 2.1.1 消费者1

```java
package com.zpc.rabbitmq.work;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.zpc.rabbitmq.util.ConnectionUtil;

public class Recv {

    private final static String QUEUE_NAME = "test_queue_work";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 同一时刻服务器只会发一条消息给消费者
        //channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，false表示手动返回完成状态，true表示自动
        channel.basicConsume(QUEUE_NAME, true, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [y] Received '" + message + "'");
            //休眠
            Thread.sleep(10);
            // 返回确认状态，注释掉表示使用自动确认模式
            //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
```

##### 2.2 消费者2

```java
package com.zpc.rabbitmq.work;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;
import com.zpc.rabbitmq.util.ConnectionUtil;

public class Recv2 {

    private final static String QUEUE_NAME = "test_queue_work";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 同一时刻服务器只会发一条消息给消费者
        //channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，false表示手动返回完成状态，true表示自动
        channel.basicConsume(QUEUE_NAME, true, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
            // 休眠1秒
            Thread.sleep(1000);
            //下面这行注释掉表示使用自动确认模式
            //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
```

##### 2.3 生产者

```java
package com.zpc.rabbitmq.work;

import com.zpc.rabbitmq.util.ConnectionUtil;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Send {

    private final static String QUEUE_NAME = "test_queue_work";

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        for (int i = 0; i < 100; i++) {
            // 消息内容
            String message = "" + i;
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");

            Thread.sleep(i * 10);
        }

        channel.close();
        connection.close();
    }
}
```



#### 2.2 能者多劳Work模式

##### 2.2.1 修改消费者Consumer

```java
// 同一时刻服务器只会发一条消息给消费者
channel.basicQos(1);


// 同时改为手动确认：
// 监听队列， false表示手动返回完成状态，true表示自动
channel.basicConsume(QUEUE_NAME, false, consumer);


// 在获取信息中
// 开启这行 表示使用手动确认模式
channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
```



#### 2.3 消费的确认模式

##### 2.3.1 自动确认

只要消息从队列中获取，无论消费者获取到消息后是否成功消息，都认为是消息已经成功消费。

![image-20240910202830338](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910202830338.png)

##### 2.3.2  手动确认

消费者从队列中获取消息后，服务器会将该消息标记为不可用状态，等待消费者的反馈，如果消费者一直没有反馈，那么该消息将一直处于不可用状态。

![image-20240910202822670](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910202822670.png)

### 3. 订阅模式（Publish/Subscribe） "fanout"

特点：

- 一个生产者，多个消费者

- 每一个消费者都有自己的一个队列
- 生产者没有将消息直接发送到队列，而是发送到了交换机
- 每个队列都要绑定到交换机
- 生产者发送的消息，经过交换机Exchange，到达队列，实现一个消息被多个消费者获取的目的

注意： 一个消费者队列可以有多个消费者实例，但是只有其中一个消费者实例会消费。

![image-20240910202902942](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910202902942.png)

交换机：

![image-20240910203655962](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910203655962.png)



#### 3.1 生产者Productor

**向交换机中发送消息。**

```java
package com.zpc.rabbitmq.subscribe;

import com.zpc.rabbitmq.util.ConnectionUtil;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class Send {

    private final static String EXCHANGE_NAME = "test_exchange_fanout";

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        // 消息内容
        String message = "Hello World!";
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}

```

**【注】：** 

- 消息发送到没有队列绑定的交换机上时，消息将丢失，因为交换机没有存储消息的能力，消息只能存储在队列中。

#### 3.2 消费者1

```java
package com.zpc.rabbitmq.subscribe;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Recv {

    private final static String QUEUE_NAME = "test_queue_work1";

    private final static String EXCHANGE_NAME = "test_exchange_fanout";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，手动返回完成
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [Recv] Received '" + message + "'");
            Thread.sleep(10);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
```

#### 3.3 消费者2

```java
package com.zpc.rabbitmq.subscribe;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Recv2 {

    private final static String QUEUE_NAME = "test_queue_work2";

    private final static String EXCHANGE_NAME = "test_exchange_fanout";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，手动返回完成
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [Recv2] Received '" + message + "'");
            Thread.sleep(10);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
```

#### 3.4 测试结果

**同一个消息被多个消费者获取。**

一个消费者队列可以有多个消费者实例，只有其中一个消费者实例会消费到消息。

在管理工具中查看队列和交换机的绑定关系：

![image-20240910205022362](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910205022362.png)

### 4. 路由模式（Routing）"direct"

<img src="C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910205516609.png" alt="image-20240910205516609" style="zoom:50%;" />





![image-20240910205529793](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910205529793.png)



#### 4.1 生产者

【注】：声明交换器时，type=direct，每条消息都会包含一个路由键（routing key），队列通过绑定键来与交换器绑定

```java
package com.zpc.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Send {

    private final static String EXCHANGE_NAME = "test_exchange_direct";

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 消息内容
        String message = "Hello World!";
        channel.basicPublish(EXCHANGE_NAME, "insert", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}
```

#### 4.2 消费者1

```java
package com.zpc.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Recv {

    private final static String QUEUE_NAME = "test_queue_work_route";

    private final static String EXCHANGE_NAME = "test_exchange_direct";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "delete");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "update");
        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，手动返回完成
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
            Thread.sleep(10);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
```



#### 4.3 消费者2

```java
package com.zpc.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Recv {

    private final static String QUEUE_NAME = "test_queue_work_route";

    private final static String EXCHANGE_NAME = "test_exchange_direct";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "delete");
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "update");
        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，手动返回完成
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
            Thread.sleep(10);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
```

#### 4.4 测试结果

生产者声明了路由键```”insert“```

![image-20240910210738942](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910210738942.png)

而只有消费者1绑定了键```insert```

![image-20240910210903590](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910210903590.png)

所以只有消费者1可以收到消息

![image-20240910210914858](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910210914858.png)

### 5. 主题模式（Topics）"topic"

![image-20240910211746690](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910211746690.png)

![image-20240910211753106](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910211753106.png)

#### 5.1 生产者

【注】：声明交换器时type=topic

```java
package com.zpc.rabbitmq.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Send {

    private final static String EXCHANGE_NAME = "test_exchange_topic";

    public static void main(String[] argv) throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        // 消息内容
        String message = "Hello World!!";
        channel.basicPublish(EXCHANGE_NAME, "routekey.1", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}


```

#### 5.2 消费者1

```java
package com.zpc.rabbitmq.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Recv {

    private final static String QUEUE_NAME = "test_queue_topic_work_1";

    private final static String EXCHANGE_NAME = "test_exchange_topic";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "routekey.*");

        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，手动返回完成
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [Recv_x] Received '" + message + "'");
            Thread.sleep(10);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}

```

#### 5.3 消费者2

```java
package com.zpc.rabbitmq.topic;

import com.zpc.rabbitmq.util.ConnectionUtil;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

public class Recv2 {

    private final static String QUEUE_NAME = "test_queue_topic_work_2";

    private final static String EXCHANGE_NAME = "test_exchange_topic";

    public static void main(String[] argv) throws Exception {

        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "*.*");

        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1);

        // 定义队列的消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 监听队列，手动返回完成
        channel.basicConsume(QUEUE_NAME, false, consumer);

        // 获取消息
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [Recv2_x] Received '" + message + "'");
            Thread.sleep(10);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}

```

#### 5.4 测试结果 

生产者声明交换器，消息内容绑定了路由键，消费者的队列只有绑定了键才可以收到这个消息

所以生产者：

![image-20240910221533165](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910221533165.png)

消费者1：

![image-20240910221555440](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910221555440.png)

消费者2：

![image-20240910221601893](C:\Users\Daily\AppData\Roaming\Typora\typora-user-images\image-20240910221601893.png)

只有消费者1的绑定键和生产者发送消息的路由键是对应的，所以只有消费者1可以成功接收消息。



## 四、Spring中rabbitmq项目

### 1. 配置文件

#### 1.1 命名空间声明

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
   xmlns:rabbit="http://www.springframework.org/schema/rabbit"
   xsi:schemaLocation="http://www.springframework.org/schema/rabbit
   http://www.springframework.org/schema/rabbit/spring-rabbit-1.4.xsd
   http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans-4.1.xsd">
```

#### 1.2 连接工厂

```xml
<!-- 定义RabbitMQ的连接工厂 host port username password virtual-host(虚拟主机,用于隔离不同的应用) -->
	<rabbit:connection-factory id="connectionFactory"
		host="123.60.179.15" port="5672" username="user01" password="123456"
		virtual-host="testhost" />
```

#### 1.3 RabbitMQ 管理

```xml
<!-- MQ的管理，包括队列、交换器等 -->
<!-- connection-factory指定连接工厂的id-，用于管理操作->
	<rabbit:admin connection-factory="connectionFactory" />
```

#### 1.4  Queue队列声明

```xml
<!-- 定义C系统需要监听的队列，自动声明 -->
<!-- name队列的名称  auto-declare=true时，则队列会在应用程序启动时自动声明。-->
<rabbit:queue name="q_topic_testC" auto-declare="true"/>
```

#### 1.5 消息监听容器

```xml
<!-- 队列监听 -->
<!-- rabbit:listener-container：定义了一个监听容器
		connection-factory 指定连接工厂的 ID 
		rabbit:listener 定义了一个监听器  ref监听器 Bean 的引用。  method监听器类中处理消息的方法名称。 queue-names监听的队列名称。
-->
	<rabbit:listener-container connection-factory="connectionFactory">
		<rabbit:listener ref="myMQlistener" method="listen" queue-names="q_topic_testC" />
	</rabbit:listener-container>
```

#### 1.6 监听器 Bean

```xml
<!-- id:Bean的ID class:监听器类的全限定名称。-->
<bean id="myMQlistener" class="com.zpc.myrabbit.listener.Listener" />
```

## 五、SpringBoot集成RabbitMQ

[RabbitMQ使用教程(超详细)-CSDN博客](https://blog.csdn.net/lzx1991610/article/details/102970854)

### 1. 简单队列

#### 1.1 配置

- 配置pom文件，主要是添加spring-boot-starter-amqp的支持

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

- 配置application.properties文件，配置rabbitmq的安装地址、端口以及账户信息

```properties
spring.application.name=spirng-boot-rabbitmq
spring.rabbitmq.host=123.60.179.15
spring.rabbitmq.port=5672
spring.rabbitmq.username=user01
spring.rabbitmq.password=123456
```

- 配置队列

```java
package com.zpc.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue queue() {
        return new Queue("q_hello",true);
    }
}

```

- 发送者 ,这里用到了(方法重载methods overloading)

```java
package com.zpc.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class HelloSender {
    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send() {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//24小时制
        String context = "hello " + date;
        System.out.println("Sender : " + context);
        //简单对列的情况下routingKey即为Q名
        this.rabbitTemplate.convertAndSend("q_hello", context);
    }

    public void send(int i) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//24小时制
        String context = "hello " + i + " " + date;
        System.out.println("Sender : " + context);
        //简单对列的情况下routingKey即为Q名
        this.rabbitTemplate.convertAndSend("q_hello", context);
    }
}

```

- 接收者

Receiver1

```java
package com.zpc.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "q_hello")
public class HelloReceiver {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("Receiver1  : " + hello);
    }
}
```

Receiver2

```java
package com.zpc.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "q_hello")
public class HelloReceiver2 {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("Receiver2  : " + hello);
    }
}
```



### 2. Topic主题队列  “topic”

topic模式是RabbitMQ最灵活的一种方式，可以根据routine_key自由的绑定不同的队列

#### 2.1 配置

- 配置队列，绑定交换机

```java
package com.zpc.rabbitmq.topic;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicRabbitConfig {

    final static String message = "q_topic_message";
    final static String messages = "q_topic_messages";

    @Bean
    public Queue queueMessage() {
        return new Queue(TopicRabbitConfig.message);
    }

    @Bean
    public Queue queueMessages() {
        return new Queue(TopicRabbitConfig.messages);
    }

    /**
     * 声明一个Topic类型的交换机
     * @return
     */
    @Bean
    TopicExchange exchange() {
        return new TopicExchange("mybootexchange");
    }

    /**
     * 绑定Q到交换机,并且指定routingKey
     * @param queueMessage
     * @param exchange
     * @return
     */
    @Bean
    Binding bindingExchangeMessage(Queue queueMessage, TopicExchange exchange) {
        return BindingBuilder.bind(queueMessage).to(exchange).with("topic.message");
    }

    @Bean
    Binding bindingExchangeMessages(Queue queueMessages, TopicExchange exchange) {
        return BindingBuilder.bind(queueMessages).to(exchange).with("topic.#");
    }
}
```

#### 2.2 生产者和消费者

- 创建(两个)消费者

Consumer1

```java
package com.zpc.rabbitmq.topic;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "q_topic_message")
public class Receiver1 {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("Receiver1  : " + hello);
    }
}


```

Consumer2

```java
package com.zpc.rabbitmq.topic;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "q_topic_messages")
public class Receiver2 {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("Receiver2 : " + hello);
    }
}


```

- 生产者

```java
package com.zpc.rabbitmq.topic;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MsgSender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send1() {
        String context = "hi, i am message 1";
        System.out.println("Sender : " + context);
        this.rabbitTemplate.convertAndSend("mybootexchange", "topic.message", context);
    }


    public void send2() {
        String context = "hi, i am messages 2";
        System.out.println("Sender : " + context);
        this.rabbitTemplate.convertAndSend("mybootexchange", "topic.messages", context);
    }
}
```

- 测试

```java
package com.zpc.rabbitmq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMqHelloTest {

    @Autowired
    private HelloSender helloSender;

    @Test
    public void hello() throws Exception {
        helloSender.send();
        Thread.sleep(200);
    }

    @Test
    public void oneToMany() throws Exception {
        for (int i=0;i<100;i++){
            helloSender.send(i);
            Thread.sleep(200);
        }
    }
}
```

#### 2.3 总结

```shell
（1）我们在TopicRabbitConfig配置类中，声明了配置规则，交换器和队列之间所约定的routingKey等
（2）在消费者，将消费者绑定到了固定的队列 consumer1（q_topic_message）  consumer2（q_topic_messages）
（3）生产者在生产消息的时候，发往交换器时，会附带routingKey，以便交换器可以根据规则发往指定的队列。

在这里，我们的交换器绑定了 队列q_topic_message和队列q_topic_messages，他们的绑定规则是，如果routingKey是topic.message，则发往队列1和队列2，如果是topic.messages，则发往队列2
```

### 3. Fanout exchange（订阅模式）“fanout”

#### 3.1 配置

```java
package com.zpc.rabbitmq.fanout;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FanoutRabbitConfig {

    @Bean
    public Queue aMessage() {
        return new Queue("q_fanout_A");
    }

    @Bean
    public Queue bMessage() {
        return new Queue("q_fanout_B");
    }

    @Bean
    public Queue cMessage() {
        return new Queue("q_fanout_C");
    }

    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange("mybootfanoutExchange");
    }

    @Bean
    Binding bindingExchangeA(Queue aMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(aMessage).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeB(Queue bMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(bMessage).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeC(Queue cMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(cMessage).to(fanoutExchange);
    }

}
```

#### 3.2 生产者消费者

- 消费者

消费者1

```java
package com.zpc.rabbitmq.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "q_fanout_A")
public class ReceiverA {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("AReceiver  : " + hello + "/n");
    }

}
```

消费者2

```java
package com.zpc.rabbitmq.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "q_fanout_B")
public class ReceiverB {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("BReceiver  : " + hello + "/n");
    }
}
```

消费者3

```java
package com.zpc.rabbitmq.fanout;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "q_fanout_C")
public class ReceiverC {

    @RabbitHandler
    public void process(String hello) {
        System.out.println("CReceiver  : " + hello + "/n");
    }

}
```

- 生产者

```java
package com.zpc.rabbitmq.fanout;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MsgSenderFanout {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send() {
        String context = "hi, fanout msg ";
        System.out.println("Sender : " + context);
        this.rabbitTemplate.convertAndSend("mybootfanoutExchange","", context);
    }
}
```

