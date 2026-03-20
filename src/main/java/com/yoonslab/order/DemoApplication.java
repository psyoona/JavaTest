package com.yoonslab.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * scanBasePackages에 com.example.common을 추가하여
 * common-web 모듈의 @RestControllerAdvice 등을 컴포넌트 스캔 대상에 포함
 */
@SpringBootApplication(scanBasePackages = {"com.yoonslab.order", "com.yoonslab.common"})
@EnableScheduling
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
