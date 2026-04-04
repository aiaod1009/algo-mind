package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan; // 加这行

// 👇 必须加这个注解！让Spring读取配置类
@SpringBootApplication
@ConfigurationPropertiesScan // ✅ 核心修复：开启配置绑定
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}