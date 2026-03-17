# Spring Boot MVC + Java 25 + PostgreSQL 18

대용량 데이터 처리에 최적화된 Spring Boot MVC 프로젝트입니다.

## 기술 스택
- **Java 25**
- **Spring Boot 3.4.4**
- **PostgreSQL 18**
- **Spring Data JPA + Hibernate**
- **Thymeleaf** (서버 사이드 렌더링)
- **Maven**

## 대용량 데이터 처리 전략

### 1. Cursor(Keyset) 기반 페이징
```sql
-- offset 기반 (느림 ❌) : 앞의 100,000건을 스캔 후 버림
SELECT * FROM orders ORDER BY id LIMIT 50 OFFSET 100000;

-- cursor 기반 (빠름 ✅) : 인덱스 range scan만 수행
SELECT * FROM orders WHERE id > 100000 ORDER BY id LIMIT 50;
```
- 데이터가 아무리 많아도 **일정한 응답 속도** 보장
- 무한 스크롤, 더보기 패턴에 적합

### 2. JPA Stream (서버 사이드 커서)
- `@QueryHints(fetchSize=500)` + `Stream<Entity>` 반환
- PostgreSQL이 서버 사이드 커서를 생성하여 500건씩 fetch
- 전체 결과를 메모리에 올리지 않으므로 **수백만 건도 처리 가능**

### 3. JDBC Batch Processing
- `JdbcTemplate`으로 `fetchSize` 설정 후 ResultSet 콜백 처리
- 배치 단위(1,000건 등)로 읽어서 처리 → 메모리 부담 최소

## 사전 요구사항

1. **Java 25** 설치
2. **PostgreSQL 18** 실행 중
3. 데이터베이스 생성:
   ```sql
   CREATE DATABASE demo;
   ```

## 실행 방법

```bash
# 빌드 및 실행
./mvnw spring-boot:run

# 또는 JAR 빌드 후 실행
./mvnw package
java -jar target/java-springboot-demo-0.0.1-SNAPSHOT.jar
```

## 엔드포인트

### 웹 UI (Thymeleaf)
| URL | 설명 |
|-----|------|
| `GET /orders` | 주문 목록 (Cursor 페이징) |
| `GET /orders/{id}` | 주문 상세 |

### REST API
| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/orders?cursor=0&size=50` | Cursor 기반 페이징 조회 |
| `GET` | `/api/orders/{id}` | 주문 상세 |
| `POST` | `/api/orders/process/stream` | Stream 기반 전체 처리 |
| `POST` | `/api/orders/process/batch?batchSize=1000` | Batch 기반 전체 처리 |
| `GET` | `/api/orders/count` | 전체 건수 조회 |

## 프로젝트 구조

```
src/main/java/com/example/demo/
├── DemoApplication.java            # 메인 애플리케이션
├── config/
│   ├── DataInitializer.java        # 샘플 데이터 초기화 (10,000건)
│   └── WebConfig.java              # MVC 설정
├── controller/
│   ├── HomeController.java         # 홈 리다이렉트
│   ├── OrderController.java        # MVC 컨트롤러 (Thymeleaf)
│   └── OrderApiController.java     # REST API 컨트롤러
├── domain/
│   └── Order.java                  # 주문 엔티티
├── dto/
│   ├── OrderDto.java               # 주문 응답 DTO
│   ├── OrderSearchCondition.java   # 검색 조건 DTO
│   └── CursorPageResponse.java     # 커서 페이징 응답 DTO
├── repository/
│   ├── OrderRepository.java        # JPA Repository (Cursor + Stream)
│   └── OrderJdbcRepository.java    # JDBC Repository (Batch 처리)
└── service/
    └── OrderService.java           # 비즈니스 로직
```
