package com.cloud.srb.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ClassName ServiceCoreApplication
 * @Author xsshuai
 * @Date 2021/4/24 8:16 下午
 **/
@SpringBootApplication
@ComponentScan({"com.cloud.srb","com.cloud.common"})
public class ServiceCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCoreApplication.class,args);
    }
}
