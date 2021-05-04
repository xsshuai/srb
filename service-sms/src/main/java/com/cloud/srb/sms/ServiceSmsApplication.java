package com.cloud.srb.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName ServiceSmsApplication
 * @Author xsshuai
 * @Date 2021/5/4 11:52 上午
 **/
@SpringBootApplication
@ComponentScan({"com.cloud.srb","com.cloud.common"})
public class ServiceSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsApplication.class,args);
    }
}
