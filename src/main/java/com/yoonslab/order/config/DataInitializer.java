package com.yoonslab.order.config;

import com.yoonslab.order.domain.Order;
import com.yoonslab.order.repository.OrderJdbcRepository;
import com.yoonslab.order.repository.OrderRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * 애플리케이션 시작 시 샘플 데이터를 생성하는 초기화 클래스
 * 대용량 데이터 테스트를 위해 10,000건의 더미 데이터를 JDBC 배치로 삽입
 */
@Component
public class DataInitializer implements ApplicationRunner {

	private static final Logger log = Logger.getLogger(DataInitializer.class.getName());

	private final OrderJdbcRepository orderJdbcRepository;
	private final OrderRepository orderRepository;

	public DataInitializer(OrderJdbcRepository orderJdbcRepository,
						   OrderRepository orderRepository) {
		this.orderJdbcRepository = orderJdbcRepository;
		this.orderRepository = orderRepository;
	}

	private static final String[] CUSTOMERS = {
			"김철수", "이영희", "박지성", "최민수", "정수연",
			"강호동", "유재석", "송혜교", "전지현", "이순신"
	};

	private static final String[] PRODUCTS = {
			"노트북", "스마트폰", "태블릿", "모니터", "키보드",
			"마우스", "헤드셋", "웹캠", "충전기", "케이스"
	};

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (orderRepository.count() > 0) {
			log.info("데이터가 이미 존재합니다. 초기화를 건너뜁니다. (현재 " + orderRepository.count() + " 건)");
			return;
		}

		log.info("샘플 데이터 생성을 시작합니다...");
		long startTime = System.currentTimeMillis();

		Random random = new Random(42);
		List<Order> batch = new ArrayList<>(1000);
		int totalCount = 10_000;

		for (int i = 0; i < totalCount; i++) {
			int qty = random.nextInt(10) + 1;
			BigDecimal price = BigDecimal.valueOf(random.nextInt(200_000) + 10_000);

			Order order = Order.builder()
					.customerName(CUSTOMERS[random.nextInt(CUSTOMERS.length)])
					.productName(PRODUCTS[random.nextInt(PRODUCTS.length)])
					.quantity(qty)
					.price(price)
					.totalAmount(price.multiply(BigDecimal.valueOf(qty)))
					.status(Order.OrderStatus.values()[random.nextInt(Order.OrderStatus.values().length)])
					.createdAt(LocalDateTime.now().minusDays(random.nextInt(365)))
					.build();

			batch.add(order);

			if (batch.size() >= 1000) {
				orderJdbcRepository.batchInsert(batch);
				batch.clear();
			}
		}

		if (!batch.isEmpty()) {
			orderJdbcRepository.batchInsert(batch);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		log.info("샘플 데이터 생성 완료: " + totalCount + " 건 (소요시간: " + elapsed + "ms)");
	}
}
