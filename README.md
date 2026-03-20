# Spring Boot + Java 25 + PostgreSQL 18

대용량 데이터 처리에 최적화된 Spring Boot 프로젝트입니다.
클라이언트 사이드 렌더링(HTML + fetch API) 방식으로, REST API를 통해 데이터를 조회합니다.

## 기술 스택
- **Java 25** (LTS)
- **Spring Boot 3.4.4**
- **PostgreSQL 18**
- **Spring Data JPA + Hibernate**
- **Gradle 9.1.0**
- **클라이언트 사이드 렌더링** (정적 HTML + JavaScript fetch API)

## 대용량 데이터 처리 전략

### 1. 번호 기반 페이징 (1~10 페이지 네비게이션)
```
GET /api/orders?page=1&size=50
GET /api/orders?page=3&size=50&customerName=홍길동
```
- Spring Data `Pageable`을 사용한 오프셋 기반 페이징
- 10개 단위 페이지 블록 계산 (1~10, 11~20, ...)
- `ORDER BY id ASC` + PK 인덱스 활용으로 성능 최적화

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

### 터미널
```bash
# 빌드 (테스트 제외)
./gradlew build -x test

# 실행
./gradlew bootRun

# JAR 빌드 후 실행
./gradlew build -x test
java -jar build/libs/demo-1.00.000.jar
```

### IntelliJ IDEA
1. **File → Open** → `JavaTest` 폴더 선택
2. Gradle 동기화 완료 대기
3. `DemoApplication.java` 열고 ▶ 버튼 클릭 (또는 `Shift+F10`)

## 엔드포인트

### 웹 UI (정적 HTML)
| URL | 설명 |
|-----|------|
| `GET /` (`index.html`) | 주문 목록 (번호 페이징) |
| `GET /detail.html?id=1` | 주문 상세 |

### REST API
| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/orders?page=1&size=50` | 번호 기반 페이징 조회 |
| `GET` | `/api/orders?page=1&size=50&customerName=홍길동&status=PENDING` | 검색 + 페이징 |
| `GET` | `/api/orders/{id}` | 주문 상세 |
| `POST` | `/api/orders/process/stream` | Stream 기반 전체 처리 |
| `POST` | `/api/orders/process/batch?batchSize=1000` | Batch 기반 전체 처리 |
| `GET` | `/api/orders/count` | 전체 건수 조회 |
| `GET` | `/api/version` | 앱 버전 정보 |

## 프로젝트 구조

```
src/main/java/com/example/demo/
├── DemoApplication.java            # 메인 애플리케이션
├── config/
│   └── DataInitializer.java        # 샘플 데이터 초기화 (10,000건)
├── controller/
│   ├── OrderApiController.java     # 주문 REST API 컨트롤러
│   └── VersionController.java      # 버전 정보 API
├── domain/
│   └── Order.java                  # 주문 엔티티
├── dto/
│   ├── OrderDto.java               # 주문 응답 DTO
│   ├── OrderSearchCondition.java   # 검색 조건 DTO
│   ├── PageResponse.java           # 번호 페이징 응답 DTO
│   └── CursorPageResponse.java     # 커서 페이징 응답 DTO
├── repository/
│   ├── OrderRepository.java        # JPA Repository (Page + Stream)
│   └── OrderJdbcRepository.java    # JDBC Repository (Batch 처리)
└── service/
    └── OrderService.java           # 비즈니스 로직

src/main/resources/
├── application.yml                 # DB·JPA 설정
└── static/
    ├── index.html                  # 주문 목록 페이지
    ├── detail.html                 # 주문 상세 페이지
    ├── css/app.css                 # 스타일시트
    └── js/
        ├── app.js                  # 목록 페이지 스크립트
        └── detail.js               # 상세 페이지 스크립트
```

## common-web 라이브러리 업데이트

이 프로젝트는 `com.yoonslab:common-web` 공통 모듈을 GitHub Packages에서 가져옵니다.

### 사전 설정 (최초 1회)

`~/.gradle/gradle.properties`에 GitHub 인증 정보를 추가합니다:

```properties
githubActor=깃허브_아이디
githubToken=ghp_xxxxxxxxxxxxxxxxxxxx
```

> 토큰은 GitHub → Settings → Developer settings → Personal access tokens에서 `read:packages` 권한으로 생성합니다.

### 버전 업데이트 방법

1. `build.gradle`에서 버전 수정:
   ```groovy
   dependencies {
       implementation 'com.yoonslab:common-web:1.01.002'  // 버전 변경
   }
   ```

2. Gradle 의존성 갱신:
   ```bash
   ./gradlew build --refresh-dependencies -x test
   ```

3. IDE를 사용하는 경우 Gradle 동기화 (IntelliJ: 🔄 아이콘 또는 `Ctrl+Shift+O`)

### 사용 가능한 버전 확인

GitHub Packages 페이지에서 확인할 수 있습니다:
- Repository: `psyoona/common-web` → Packages 탭

## 버전
- **현재 버전**: `1.00.000`
- `GET /api/version`으로 확인 가능
- `build.gradle`의 `version` 속성 + `springBoot { buildInfo() }`로 관리
