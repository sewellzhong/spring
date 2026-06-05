package com.sewellzhong.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ComponentScan("com.sewellzhong.selftag")
public class MyComponentScan {

    @ComponentScan("com.sewellzhong.selftag")
    @Configuration
    @Order(90)
    class InnerClass{

    }

}
