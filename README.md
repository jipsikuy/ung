# UNG - QueryDSL and SQL Data Synchronization

이 프로젝트는 QueryDSL과 SQL 처리를 기반으로 한 데이터 동기화 및 로직 병합 구현을 제공합니다.

## 주요 기능

### 1. 이중 구현 방식 (Dual Implementation)
- **QueryDSL 기반**: 타입 안전한 동적 쿼리 처리
- **SQL 기반**: 네이티브 SQL을 통한 병렬 처리
- 두 구현 방식의 결과 일관성 검증

### 2. DATA_REL 검증 로직
- 관계 데이터 검증
- 중복 관계 탐지
- 관계 일관성 확인
- 우선순위 기반 정렬

### 3. 동기화된 검증 구조
- `DataRelSyncService`: 두 구현의 동시 실행 및 결과 비교
- 일관성 검증 메커니즘
- 실시간 동기화 상태 모니터링

## 프로젝트 구조

```
src/main/java/com/ung/
├── entity/
│   └── DataRel.java              # DATA_REL 엔티티
├── repository/
│   ├── DataRelRepository.java           # JPA Repository
│   ├── DataRelQueryDslRepository.java   # QueryDSL 구현
│   └── DataRelSqlRepository.java        # SQL 구현
├── service/
│   └── DataRelSyncService.java          # 동기화 서비스
├── config/
│   └── QueryDslConfig.java              # QueryDSL 설정
└── UngApplication.java                   # Main Application

src/test/java/com/ung/
└── service/
    └── DataRelSyncServiceTest.java      # 통합 테스트
```

## 핵심 컴포넌트

### DataRel Entity
관계 데이터를 표현하는 핵심 엔티티:
- `sourceId`: 소스 ID
- `targetId`: 타겟 ID
- `relType`: 관계 유형
- `status`: 상태 (ACTIVE, INACTIVE 등)
- `priority`: 우선순위
- `isValid`: 유효성 플래그

### DataRelQueryDslRepository
QueryDSL을 사용한 동적 쿼리 구현:
- `validateDataRel()`: 동적 조건으로 DATA_REL 검증
- `findByComplexConditions()`: 복잡한 조건 검색
- `validateRelationshipConsistency()`: 관계 일관성 검증
- `findDuplicateRelationships()`: 중복 관계 탐지

### DataRelSqlRepository
네이티브 SQL을 사용한 병렬 구현:
- QueryDSL과 동일한 메서드 시그니처
- 네이티브 SQL 쿼리 사용
- 동적 SQL 생성

### DataRelSyncService
동기화 서비스:
- `validateDataRelSync()`: 두 구현의 동시 검증
- `validateComplexConditions()`: 복잡한 조건 동기화 검증
- `checkRelationshipConsistency()`: 일관성 확인
- `findDuplicatesSync()`: 중복 동기화 탐지

## 빌드 및 실행

### 요구사항
- Java 17 이상
- Maven 3.6 이상

### 빌드
```bash
mvn clean install
```

### QueryDSL Q-클래스 생성
```bash
mvn compile
```
이 명령은 `QDataRel` 클래스를 `target/generated-sources/java`에 생성합니다.

### 테스트 실행
```bash
mvn test
```

### 애플리케이션 실행
```bash
mvn spring-boot:run
```

## 사용 예제

### 1. 기본 검증
```java
@Autowired
private DataRelSyncService syncService;

// QueryDSL과 SQL 양쪽으로 검증
ValidationResult result = syncService.validateDataRelSync(
    "SRC001", "TGT001", "PARENT_CHILD", "ACTIVE"
);

// 결과 일관성 확인
boolean isConsistent = result.isConsistent();
List<DataRel> queryDslResults = result.getQueryDslResults();
List<DataRel> sqlResults = result.getSqlResults();
```

### 2. 복잡한 조건 검증
```java
// 특정 관계 유형, 최소 우선순위로 검증
ValidationResult result = syncService.validateComplexConditions(
    "PARENT_CHILD", 5, LocalDateTime.now().minusDays(7)
);
```

### 3. 관계 일관성 확인
```java
ConsistencyCheckResult result = syncService.checkRelationshipConsistency(
    "SRC001", "TGT001"
);

boolean isValid = result.isQueryDslValid() && result.isSqlValid();
```

### 4. 중복 탐지
```java
ValidationResult duplicates = syncService.findDuplicatesSync(
    "SRC001", "TGT001", "PARENT_CHILD"
);
```

## 기술 스택

- **Spring Boot 3.1.5**: 애플리케이션 프레임워크
- **Spring Data JPA**: 데이터 접근 계층
- **QueryDSL 5.1.0**: 타입 안전 쿼리
- **H2 Database**: 인메모리 데이터베이스 (개발/테스트용)
- **Lombok**: 보일러플레이트 코드 감소
- **JUnit 5**: 테스트 프레임워크

## 보안 (Security)

QueryDSL 5.1.0에 알려진 HQL injection 취약점이 있습니다. 하지만 **본 구현은 안전합니다**:
- ✅ 사용자 입력을 orderBy에 사용하지 않음
- ✅ 모든 정렬은 정적이고 컴파일 타임에 정의됨
- ✅ 타입 안전 쿼리 구성 사용
- ✅ 모든 동적 입력은 파라미터화됨

자세한 내용은 [SECURITY.md](SECURITY.md)를 참조하세요.

## 테스트 커버리지

통합 테스트 (`DataRelSyncServiceTest`)는 다음을 검증합니다:
- ✅ QueryDSL과 SQL 구현의 결과 일치
- ✅ 동적 쿼리 조건 처리
- ✅ 우선순위 기반 정렬
- ✅ 관계 일관성 검증
- ✅ 중복 관계 탐지
- ✅ 날짜 필터링

## 구현 특징

### 동적 쿼리 처리
- QueryDSL의 `BooleanBuilder`를 사용한 조건 동적 구성
- SQL의 동적 문자열 빌딩

### 결과 일관성 보장
- 두 구현의 결과 개수 비교
- 결과 ID 순서 비교
- 불일치 시 로그 경고

### 검증 구조
- `ValidationResult`: 두 구현의 결과와 일관성 상태 포함
- `ConsistencyCheckResult`: 일관성 검사 결과
- 타임스탬프를 통한 검증 시점 추적

## 향후 확장 가능성

1. **추가 검증 로직**: 더 복잡한 비즈니스 규칙 구현
2. **성능 모니터링**: 두 구현의 성능 비교
3. **캐싱 계층**: 자주 조회되는 관계 데이터 캐싱
4. **비동기 처리**: 대량 데이터 처리 시 비동기 검증
5. **메트릭 수집**: 일관성 불일치 발생 빈도 추적

## 라이선스

MIT License