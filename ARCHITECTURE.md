# Architecture Documentation

## 개요 (Overview)

이 프로젝트는 QueryDSL과 네이티브 SQL을 동시에 사용하여 데이터 검증 로직을 이중으로 구현하고, 두 구현의 결과가 일치하는지 확인하는 동기화 시스템입니다.

## 아키텍처 (Architecture)

### 계층 구조 (Layer Structure)

```
┌─────────────────────────────────────────┐
│      Service Layer (동기화 계층)         │
│   DataRelSyncService                    │
│   - 두 구현 방식 동시 실행                │
│   - 결과 일관성 검증                     │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
┌─────────────┐  ┌─────────────┐
│ QueryDSL    │  │ SQL         │
│ Repository  │  │ Repository  │
└──────┬──────┘  └──────┬──────┘
       │                │
       └───────┬────────┘
               ▼
      ┌────────────────┐
      │ JPA Repository │
      └────────┬───────┘
               ▼
      ┌────────────────┐
      │  Entity Layer  │
      │   DataRel      │
      └────────────────┘
```

### 주요 컴포넌트 (Key Components)

#### 1. Entity Layer - DataRel

**위치**: `com.ung.entity.DataRel`

**역할**: 관계 데이터를 표현하는 JPA 엔티티

**주요 필드**:
- `sourceId`: 관계의 시작점 ID
- `targetId`: 관계의 목적지 ID
- `relType`: 관계 유형 (PARENT_CHILD, SIBLING 등)
- `status`: 관계 상태 (ACTIVE, INACTIVE 등)
- `priority`: 우선순위 (정렬에 사용)
- `isValid`: 유효성 플래그
- `metadata`: JSON 형식의 추가 정보

#### 2. Repository Layer

##### 2.1 DataRelRepository

**위치**: `com.ung.repository.DataRelRepository`

**역할**: Spring Data JPA 기본 리포지토리 + QueryDSL 지원

**특징**:
- `JpaRepository<DataRel, Long>` 상속
- `QuerydslPredicateExecutor<DataRel>` 상속
- 기본 CRUD 및 QueryDSL Predicate 실행 지원

##### 2.2 DataRelQueryDslRepository

**위치**: `com.ung.repository.DataRelQueryDslRepository`

**역할**: QueryDSL 기반 동적 쿼리 구현

**핵심 메서드**:

```java
// 1. 동적 조건 검증
List<DataRel> validateDataRel(String sourceId, String targetId, 
                               String relType, String status)
```
- `BooleanBuilder`를 사용한 동적 조건 구성
- null 파라미터는 조건에서 제외
- 항상 `isValid = true` 조건 포함
- 우선순위 및 생성일시 기준 정렬

```java
// 2. 복잡한 조건 검색
List<DataRel> findByComplexConditions(String relType, 
                                       Integer minPriority, 
                                       LocalDateTime startDate)
```
- 여러 조건의 조합 처리
- 우선순위 범위 검색 (`>=` 조건)
- 날짜 범위 검색

```java
// 3. 관계 일관성 검증
boolean validateRelationshipConsistency(String sourceId, String targetId)
```
- 특정 관계의 존재 여부 확인
- ACTIVE 상태만 검증

```java
// 4. 중복 관계 탐지
List<DataRel> findDuplicateRelationships(String sourceId, 
                                          String targetId, 
                                          String relType)
```
- 동일한 관계 정의를 가진 레코드 검색

**구현 특징**:
- `JPAQueryFactory` 사용
- 타입 안전성 보장 (컴파일 타임 검증)
- 생성된 `QDataRel` 클래스 활용

##### 2.3 DataRelSqlRepository

**위치**: `com.ung.repository.DataRelSqlRepository`

**역할**: 네이티브 SQL 기반 구현 (QueryDSL과 동일한 메서드 시그니처)

**핵심 메서드**:
- `validateDataRel()`: 동적 SQL 문자열 생성
- `findByComplexConditions()`: WHERE 절 동적 구성
- `validateRelationshipConsistency()`: COUNT 쿼리
- `findDuplicateRelationships()`: JOIN 없는 단순 검색

**구현 특징**:
- `EntityManager.createNativeQuery()` 사용
- 동적 SQL 문자열 빌딩
- Named 파라미터 바인딩
- 결과를 `DataRel` 엔티티로 매핑

#### 3. Service Layer - DataRelSyncService

**위치**: `com.ung.service.DataRelSyncService`

**역할**: 두 구현 방식의 동기화 및 일관성 검증

**핵심 메서드**:

```java
ValidationResult validateDataRelSync(String sourceId, String targetId,
                                      String relType, String status)
```
**동작 방식**:
1. QueryDSL 구현 실행
2. SQL 구현 실행 (병렬 개념)
3. 결과 비교 (`compareResults()`)
4. `ValidationResult` 반환

**결과 비교 로직**:
- 결과 개수 비교
- 각 레코드의 ID 순서 비교
- 불일치 시 로그 경고

```java
ConsistencyCheckResult checkRelationshipConsistency(String sourceId, 
                                                     String targetId)
```
**동작 방식**:
1. QueryDSL과 SQL로 각각 일관성 확인
2. 두 결과가 같은지 검증
3. `ConsistencyCheckResult` 반환

### 데이터 흐름 (Data Flow)

```
클라이언트 요청
    ↓
DataRelSyncService
    ├─→ DataRelQueryDslRepository
    │       ├─→ JPAQueryFactory
    │       ├─→ BooleanBuilder (동적 조건)
    │       ├─→ QDataRel (생성된 메타모델)
    │       └─→ 결과 A
    │
    ├─→ DataRelSqlRepository
    │       ├─→ EntityManager
    │       ├─→ Native SQL (동적 생성)
    │       └─→ 결과 B
    │
    └─→ 결과 비교 (A == B ?)
            ├─→ 일치: isConsistent = true
            └─→ 불일치: isConsistent = false + 로그
```

### 설정 (Configuration)

#### QueryDslConfig

**위치**: `com.ung.config.QueryDslConfig`

**역할**: QueryDSL 설정

```java
@Bean
public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
}
```

### 데이터베이스 스키마

```sql
CREATE TABLE data_rel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id VARCHAR(255) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    rel_type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    priority INT,
    metadata VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_valid BOOLEAN NOT NULL
);

-- 인덱스 (성능 최적화)
CREATE INDEX idx_source_id ON data_rel(source_id);
CREATE INDEX idx_target_id ON data_rel(target_id);
CREATE INDEX idx_rel_type ON data_rel(rel_type);
CREATE INDEX idx_status ON data_rel(status);
CREATE INDEX idx_is_valid ON data_rel(is_valid);
```

## 동기화 메커니즘 (Synchronization Mechanism)

### 일관성 보장

1. **동일한 메서드 시그니처**: QueryDSL과 SQL 구현이 같은 입력을 받음
2. **동일한 비즈니스 로직**: 두 구현 모두 같은 조건 적용
3. **결과 비교**: 개수와 순서 검증
4. **로깅**: 불일치 발생 시 경고 로그

### 검증 레벨

#### Level 1: 데이터 검증 (validateDataRelSync)
- 동적 조건으로 데이터 조회
- 두 구현의 결과 집합 비교

#### Level 2: 복잡 조건 검증 (validateComplexConditions)
- 여러 조건 조합
- 범위 검색 및 날짜 필터

#### Level 3: 일관성 검증 (checkRelationshipConsistency)
- 존재 여부만 확인 (boolean)
- 두 구현의 판단 일치 여부

#### Level 4: 중복 탐지 (findDuplicatesSync)
- 중복 레코드 검색
- 결과 일치 확인

## 테스트 전략 (Testing Strategy)

### 통합 테스트

**위치**: `com.ung.service.DataRelSyncServiceTest`

**커버리지**:
- ✅ 모든 파라미터 조합 테스트
- ✅ 동적 조건 처리 검증
- ✅ 우선순위 정렬 확인
- ✅ 날짜 필터링 검증
- ✅ 일관성 체크 시나리오
- ✅ 중복 탐지 로직

**테스트 데이터 구조**:
```java
@BeforeEach
void setUp() {
    // 테스트용 데이터 생성
    // - 다양한 relType
    // - 다양한 priority
    // - 유효/무효 데이터 혼합
}
```

## 확장 가능성 (Extensibility)

### 1. 새로운 검증 로직 추가

```java
// QueryDSL 구현
public List<DataRel> newValidationLogic(...) {
    // QueryDSL 쿼리
}

// SQL 구현
public List<DataRel> newValidationLogic(...) {
    // Native SQL 쿼리
}

// 동기화 서비스
public ValidationResult newValidationSync(...) {
    // 두 구현 실행 및 비교
}
```

### 2. 성능 모니터링 추가

```java
@Around("execution(* DataRelSyncService.*(..))")
public Object measurePerformance(ProceedingJoinPoint pjp) {
    // QueryDSL 실행 시간 측정
    // SQL 실행 시간 측정
    // 비교 및 로그
}
```

### 3. 캐싱 계층 추가

```java
@Cacheable(value = "dataRelCache", key = "#sourceId + #targetId")
public ValidationResult validateDataRelSync(...) {
    // 캐시 적용
}
```

## 모범 사례 (Best Practices)

1. **항상 두 구현을 동시에 업데이트**: 한쪽만 변경하면 불일치 발생
2. **테스트 우선 개발**: 새로운 검증 로직 추가 시 테스트 먼저 작성
3. **로그 모니터링**: 불일치 발생 시 즉시 확인 및 수정
4. **성능 측정**: 두 구현의 성능 차이 모니터링
5. **트랜잭션 관리**: 읽기 전용 작업은 `@Transactional(readOnly = true)` 사용

## 문제 해결 (Troubleshooting)

### Q: QueryDSL Q-클래스가 생성되지 않음

**A**: Maven 컴파일 실행
```bash
mvn clean compile
```

### Q: 일관성 불일치 발생

**A**: 로그 확인 및 두 구현의 쿼리 조건 비교
```java
log.warn("Result count mismatch: QueryDSL={}, SQL={}", ...);
```

### Q: 성능 차이 발생

**A**: 
- 데이터베이스 인덱스 확인
- 쿼리 실행 계획 분석
- 필요시 쿼리 최적화
