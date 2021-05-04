package com.cloud.srb.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName ServiceOssApplication
 * @Author xsshuai
 * @Date 2021/5/4 5:01 下午
 **/
@SpringBootApplication
@ComponentScan({"com.cloud.srb", "com.cloud.common"})
public class ServiceOssApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOssApplication.class, args);
    }
}
