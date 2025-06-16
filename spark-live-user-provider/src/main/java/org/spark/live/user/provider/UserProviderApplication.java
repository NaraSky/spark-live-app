package org.spark.live.user.provider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务提供者应用程序入口类
 *
 * @SpringBootApplication: 标识这是一个Spring Boot应用程序，包含了@Configuration、@EnableAutoConfiguration和@ComponentScan
 * @EnableDubbo: 启用Dubbo服务，支持服务的注册与发现
 * @EnableDiscoveryClient: 启用服务发现客户端，使该服务可以被注册到服务注册中心
 */
@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class UserProviderApplication {
    public static void main(String[] args) {
        // 创建Spring应用程序实例
        SpringApplication application = new SpringApplication(UserProviderApplication.class);
        // 设置应用程序类型为非Web应用，因为这是一个服务提供者，不需要提供Web界面
        application.setWebApplicationType(WebApplicationType.NONE);
        // 启动应用程序
        application.run(args);
    }
}
