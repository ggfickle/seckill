package org.apache.rocketmq.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.retry.annotation.EnableRetry;

/**
 * @author snowalker
 * @date 2019/6/14 13:46
 * 秒杀订单服务启动器
 */
@ImportResource({"classpath*:META-INF/applicationContext-bean.xml"})
@SpringBootApplication
@EnableRetry
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
