# ADR-001 — Bike Dashboard Backend Architecture Decision Record

| 항목 | 내용 |
|---|---|
| **프로젝트** | metanet-spring-mini-project |
| **작성일** | 2026-04-08 |
| **작성자** | CHO YONG JIN |
| **상태** | Accepted |

---

## 배경 (Context)

Eclipse STS 4 환경에서 Spring Boot 3.x 기반의 공공자전거 대시보드 백엔드를 구축하는 과정에서, IDE 충돌 · SSE 다중 접속 · Oracle DB 통계 쿼리 · 반복 조회 성능이라는 4가지 기술적 도전이 동시에 발생하였다.  
본 문서는 각 문제에 대한 원인 분석 및 최종 아키텍처 결정 근거를 기록한다.

---

## ADR-001-1 | 환경 구성 — Gradle Toolchain 충돌 및 Eclipse 메타데이터 동기화

### 문제 상황

`build.gradle`에 선언된 `java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }` 블록은 Gradle Toolchain 기능을 활성화하여, 빌드 시 로컬에 설치된 JDK가 아닌 **Gradle이 관리하는 별도 JDK를 자동 탐색 및 다운로드**하려 시도한다.  
로컬 환경에 Toolchain provisioner(`foojay-resolver`) 설정이 없으면 JDK 탐색이 실패하거나 빌드 시간이 비정상적으로 증가하며, Eclipse `.classpath` 메타데이터가 IDE의 JRE와 충돌하여 컴파일 오류 및 빨간 줄(빌드 패스 에러)이 발생한다.

추가로, `id 'eclipse'` 플러그인이 없으면 `./gradlew eclipse` 태스크 자체가 존재하지 않아 IDE 메타데이터를 재생성할 수 없다.

### 결정 (Decision)

```groovy
// Before — Toolchain (환경 의존성 높음)
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// After — sourceCompatibility (로컬 JDK 직접 사용)
sourceCompatibility = '17'
```

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.13'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'eclipse'   // ← 추가: cleanEclipse / eclipse 태스크 활성화
}
```

Toolchain 대신 `sourceCompatibility`를 명시적으로 선언하면 Gradle은 `JAVA_HOME` 또는 `PATH`에 등록된 **로컬 JDK 17을 그대로 사용**하므로, 프로비저닝 실패 없이 즉시 빌드가 가능하다.

### 운영 절차

IDE 의존성이 변경될 때마다 아래 명령으로 `.classpath` · `.project` · `.settings/` 메타데이터를 완전 재생성한다.

```bash
./gradlew cleanEclipse eclipse
```

### 결과 (Consequence)

- 빌드 환경에 관계없이 로컬 JDK 17로 일관된 컴파일 보장
- `cleanEclipse eclipse`를 통한 Eclipse 메타데이터 단일 명령 초기화

---

## ADR-001-2 | IDE 버그 우회 — STS "Model not available" 에러와 `.launch` 파일 전략

### 문제 상황

Eclipse STS의 Spring Boot Dashboard는 실행 설정(Run Configuration)을 **동적으로 모델에서 참조**한다.  
Git 리셋 · 브랜치 초기화 · 워크스페이스 재임포트 이후에는 STS 내부 모델 캐시가 무효화되어 `"Model not available"` 에러가 발생하며, Dashboard에서 앱 실행이 불가능해진다.

이는 STS 고유의 런타임 버그로, IDE 재설치나 워크스페이스 재구성 없이는 재현 조건을 통제하기 어렵다.

### 결정 (Decision)

프로젝트 루트에 **정적 `.launch` XML 파일**을 커밋하여 Spring Boot Dashboard 의존성을 완전히 우회한다.

```xml
<launchConfiguration
    type="org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate">
    <stringAttribute
        key="org.eclipse.jdt.launching.MAIN_TYPE"
        value="com.dashboard.app.AppApplication"/>
    <stringAttribute
        key="org.eclipse.jdt.launching.PROJECT_ATTR"
        value="app"/>
</launchConfiguration>
```

`.launch` 파일은 STS가 모델 캐시 없이도 읽을 수 있는 **선언적 실행 설정**이다.  
파일을 더블클릭하면 IDE가 XML을 직접 파싱해 Run Configuration을 복원하므로, 동적 모델 참조 경로를 완전히 우회한다.

### 보안 고려사항

`.launch` 파일은 로컬 경로 정보를 포함할 수 있으므로 `.gitignore`에 `*.launch`를 추가하여 원격 저장소 노출을 차단한다.

```
# .gitignore
*.launch
```

### 결과

- Git 리셋 이후에도 F5 → 더블클릭으로 즉시 서버 기동 가능
- STS 모델 캐시 상태와 완전히 독립된 실행 경로 확보

---

## ADR-001-3 | 실시간 통신 — SSE 다중 접속 환경의 스레드 안전성 및 메모리 누수 방지

### 문제 상황

HTTP 기반의 실시간 스트리밍에서 SSE(Server-Sent Events)는 WebSocket 대비 **단방향 · 자동 재연결 · HTTP/1.1 호환**이라는 이점으로 대시보드 시나리오에 적합하다.  
그러나 다중 클라이언트 환경에서 `SseEmitter` 인스턴스를 관리하는 컬렉션에 대한 동시 읽기/쓰기가 발생하면 `ConcurrentModificationException`이 발생할 수 있고, 연결이 끊긴 emitter가 컬렉션에 잔존하면 메모리 누수로 이어진다.

### 결정 (Decision)

**① 컬렉션 전략 — `CopyOnWriteArrayList`**

```java
private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
```

`CopyOnWriteArrayList`는 쓰기(add/remove) 시 내부 배열을 복사하여 새 인스턴스로 교체한다.  
읽기 스레드는 교체 전 스냅샷을 사용하므로 **락 없이 동시 순회가 안전**하다.  
대시보드 특성상 읽기(브로드캐스트 순회)가 쓰기(접속/해제)보다 압도적으로 많아 이 트레이드오프는 유리하다.

**② 생명주기 콜백 — 3중 방어선**

```java
emitter.onTimeout(()    -> emitters.remove(emitter));  // 10분 만료
emitter.onCompletion(() -> emitters.remove(emitter));  // 정상 종료
emitter.onError(e       -> emitters.remove(emitter));  // 네트워크 오류
```

Servlet 컨테이너(Tomcat)는 emitter 생명주기 이벤트를 비동기 콜백으로 통지한다.  
3가지 모든 종료 경로에 `remove`를 등록하여, **어떤 방식으로 연결이 끊겨도** 컬렉션에서 즉시 제거되도록 설계한다.

**③ 503 방지 — 초기 더미 이벤트**

```java
emitter.send(SseEmitter.event().name("connect").data("SSE connected"));
```

SSE 연결 수립 즉시 이벤트를 발송하지 않으면 일부 프록시/로드밸런서가 응답 없음으로 판단하여 503을 반환한다.  
연결 직후 더미 이벤트를 선제 발송하여 이를 방지한다.

**④ 브로드캐스트 시 데드 Emitter 즉시 정리**

```java
List<SseEmitter> deadEmitters = new ArrayList<>();
for (SseEmitter emitter : emitters) {
    try {
        emitter.send(...);
    } catch (IOException e) {
        deadEmitters.add(emitter);   // 전송 실패 = 연결 끊김
    }
}
emitters.removeAll(deadEmitters);
```

콜백이 누락된 edge case를 대비하여, 브로드캐스트 순회 중 `IOException`이 발생한 emitter도 즉시 수거한다.

### 결과

- 동시 접속 N명 환경에서 `ConcurrentModificationException` 완전 차단
- 콜백 3중 방어 + 브로드캐스트 시 수거로 **메모리 누수 이중 방지**
- 타임아웃 10분(`600_000ms`) 설정으로 불필요한 좀비 연결 자동 해제

---

## ADR-001-4 | DB 아키텍처 — Oracle Native Query + Projection 인터페이스 매핑 전략

### 문제 상황

`GROUP BY` / `SUM` / `FETCH FIRST N ROWS ONLY` 등의 Oracle 전용 집계 쿼리는 JPQL로 표현하기 어렵거나 불가능하다.  
ORM 엔티티가 아닌 **집계 결과(Aggregated Result Set)** 를 DTO로 매핑하려면 `Object[]` 배열 캐스팅이나 `@SqlResultSetMapping` 같은 복잡한 설정이 필요했다.

### 결정 (Decision)

**① `@Query(nativeQuery = true)` — Oracle SQL 직접 사용**

```java
@Query(nativeQuery = true, value =
    "SELECT STATION_NAME   AS \"stationName\", " +
    "       SUM(USE_COUNT) AS \"totalCount\"   " +
    "FROM   BIKE                               " +
    "GROUP BY STATION_NAME                     " +
    "ORDER BY SUM(USE_COUNT) DESC              " +
    "FETCH FIRST 10 ROWS ONLY                 ")
List<TopStationDto> findTopStations();
```

JPQL 변환 없이 Oracle SQL을 그대로 실행하므로 `FETCH FIRST`, `TO_CHAR`, `NVL` 등 DB 전용 함수를 자유롭게 사용한다.

**② Spring Data Projection 인터페이스 — 무설정 결과 매핑**

```java
public interface TopStationDto {
    String getStationName();
    Long   getTotalCount();
}
```

Spring Data JPA는 `ResultSet`의 컬럼 레이블(별칭)을 Projection 인터페이스의 getter 이름과 자동 매핑한다.  
별도의 `@SqlResultSetMapping` 설정이나 `RowMapper` 구현 없이 **컴파일 타임에 타입 안전한 매핑**이 완성된다.

**③ Oracle 대소문자 별칭 고정 — 큰따옴표(double-quote) 전략**

Oracle은 인용부호 없는 식별자를 내부적으로 대문자로 강제 변환한다.  
`AS stationName` 으로 선언해도 실제 컬럼 레이블은 `STATIONNAME`이 되어 Projection 매핑이 실패한다.

```sql
-- ❌ Oracle이 STATIONNAME 으로 변환 → getter 매핑 실패
SELECT STATION_NAME AS stationName

-- ✅ 큰따옴표로 소문자 고정 → getter getStationName() 정확히 매핑
SELECT STATION_NAME AS "stationName"
```

**④ 단일 엔드포인트 동적 라우팅 — `@PathVariable` + switch expression**

```java
// GET /bike/stats/{chart-type}
public List<?> getChartData(String chartType) {
    return switch (chartType) {
        case "top-stations" -> bikeRepository.findTopStations();
        case "demographics" -> bikeRepository.findDemographics();
        case "daily-trend"  -> bikeRepository.findDailyTrend();
        default -> throw new IllegalArgumentException("지원하지 않는 차트 타입: " + chartType);
    };
}
```

차트 종류가 늘어나도 컨트롤러 레이어를 수정하지 않고 Service의 switch와 Repository의 메서드만 확장하면 된다.  
`List<?>`를 반환하고 `ResponseEntity<?>`로 래핑하여 컨트롤러는 타입에 완전히 무관해진다.

### 결과

- Oracle 방언(Dialect) 쿼리를 추상화 손실 없이 그대로 실행
- Projection 인터페이스로 `Object[]` 캐스팅 없는 타입 안전 매핑
- 단일 URL + switch 라우팅으로 차트 종류 확장 시 컨트롤러 변경 불필요

---

## ADR-001-5 | 성능 최적화 — @Cacheable 로컬 캐싱 + @Scheduled 브로드캐스팅 전체 흐름

### 문제 상황

지도 데이터(스테이션 목록)는 대시보드 UI가 렌더링될 때마다 호출되지만 실시간 변동이 없다.  
매 요청마다 Oracle로 SELECT를 보내는 것은 네트워크 RTT와 DB 커넥션 풀을 낭비한다.

반대로 접속자 수는 매 요청마다 `emitters.size()`로 계산하는 것보다 일정 주기로 전체에 푸시하는 것이 서버 자원 효율이 높다.

### 결정 (Decision)

**① `@Cacheable` — 요청 경로 캐싱 (Pull 모델)**

```
클라이언트 요청
    │
    ▼
BikeController.getMapData()
    │
    ▼
Spring Cache Interceptor ──[캐시 HIT]──▶ 캐시에서 즉시 반환
    │ [캐시 MISS]
    ▼
BikeService.getMapData()  ← @Cacheable("bikeMap")
    │
    ▼
인메모리 데이터 or DB 조회
    │
    ▼ (결과를 "bikeMap" 캐시에 저장)
클라이언트 응답
```

`@EnableCaching`으로 활성화된 `ConcurrentMapCacheManager`가 힙 메모리에 캐시를 관리한다.  
첫 번째 요청 이후 `"bikeMap"` 키에 결과가 저장되며, 이후 모든 요청은 DB를 거치지 않고 즉시 반환된다.

별도 캐시 서버(Redis 등) 없이 로컬 JVM 메모리만으로 동작하므로 인프라 의존성이 없다.

**② `@Scheduled` — 능동적 브로드캐스팅 (Push 모델)**

```
Spring Scheduler (별도 스레드)
    │ 10초마다 실행
    ▼
BikeService.broadcastUserCount()
    │
    ├─ emitters.size() → 현재 접속자 수 계산
    │
    ├─ for (SseEmitter emitter : emitters)
    │       emitter.send(event("userCount").data(count))
    │                │
    │                ▼
    │          각 클라이언트의 EventSource
    │          → 브라우저 콜백 실행
    │
    └─ IOException 발생 시 → deadEmitters 수거
```

접속자 수 집계는 클라이언트가 요청하지 않아도 서버가 주기적으로 계산하고 푸시한다.  
`@Scheduled(fixedRate = 10_000)`는 이전 실행 완료 여부와 무관하게 10초 간격으로 고정 실행되어 일정한 브로드캐스트 주기를 보장한다.

**③ 전체 데이터 흐름 요약**

```
[브라우저]                        [Spring Boot Server]             [Oracle DB]
   │                                      │                            │
   │── GET /bike/connect ────────────────▶│                            │
   │◀── SSE 연결 수립 (더미 이벤트) ──────│                            │
   │                                      │                            │
   │                             [10초 스케줄러 실행]                  │
   │◀── event: userCount, data: N ────────│                            │
   │                                      │                            │
   │── GET /bike/stats/top-stations ─────▶│                            │
   │                                      │── Native Query ───────────▶│
   │                                      │◀── ResultSet ──────────────│
   │◀── JSON (Projection 직렬화) ─────────│                            │
   │                                      │                            │
   │── GET /bike/map ────────────────────▶│                            │
   │                                      │── @Cacheable HIT?          │
   │                                      │   YES → 캐시 즉시 반환     │
   │                                      │   NO  → DB 조회 + 캐시 저장│
   │◀── JSON (BikeMapDto 직렬화) ─────────│                            │
```

### 결과

- 지도 데이터: DB 조회 1회 → 이후 O(1) 캐시 조회로 응답 시간 단축
- 접속자 수: 클라이언트 폴링 없이 서버 주도 Push로 HTTP 요청 트래픽 제거
- 두 최적화 모두 인프라 추가 없이 Spring 단일 프레임워크 내에서 완결

---

## 최종 아키텍처 요약

```
┌────────────────────────────────────────────────────────┐
│                    Presentation Layer                  │
│  BikeController  (/bike/connect · /stats/{type} · /map)│
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│                    Application Layer                   │
│  BikeService                                           │
│  ├── SSE 생명주기 (CopyOnWriteArrayList + 3중 콜백)    │
│  ├── 차트 라우팅 (switch expression → Repository)      │
│  ├── @Scheduled 브로드캐스트 (10s fixedRate)           │
│  └── @Cacheable 지도 데이터 ("bikeMap")                │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│                  Infrastructure Layer                  │
│  BikeRepository (JpaRepository + Native Query 3종)     │
│  ├── findTopStations()   → TopStationDto  Projection   │
│  ├── findDemographics()  → DemographicsDto Projection  │
│  └── findDailyTrend()    → DailyTrendDto  Projection   │
└──────────────────────────┬─────────────────────────────┘
                           │
                    ┌──────▼──────┐
                    │  Oracle DB  │
                    │  BIKE Table │
                    └─────────────┘
```
