package com.community.service.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.community.service.mybatis")
public class MyBatisPlusConfig {
}

