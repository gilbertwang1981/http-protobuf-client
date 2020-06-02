package com.hs.http.client.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.hs.http.client.annotation.ServiceStartup;

@SpringBootApplication
@ComponentScan(basePackages="com.hs")
@ServiceStartup(service = "test-service" , desc = "测试服务")
public class HttpClientTest {
	public static void main(String [] args) {
		SpringApplication.run(HttpClientTest.class , args);
	}
}
