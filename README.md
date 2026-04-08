# Metanet Spring Mini Project — Bike Dashboard Backend

> 실시간 공공자전거 대시보드 REST API 서버  
> Spring Boot 3.x · Oracle DB · SSE · Spring Data JPA · Spring Cache

---

## 목차
1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [API 엔드포인트](#3-api-엔드포인트)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [로컬 실행 방법](#5-로컬-실행-방법)
6. [Architecture Decision Records](#6-architecture-decision-records)

---

## 1. 프로젝트 개요

Oracle DB에 적재된 공공자전거(`BIKE`) 이용 데이터를 기반으로,
대시보드 UI에 차트 데이터를 제공하는 백엔드 API 서버입니다.

- **실시간 접속자 수** SSE 스트리밍
- **3종 차트 통계** (상위 대여소 / 인구통계 / 일별 추이) REST API
- **지도 데이터** 캐시 기반 제공

---

## 2. 기술 스택

| 분류 | 기술 |
|---|---|
| Framework | Spring Boot 3.5.x |
| Language | Java 17 |
| ORM | Spring Data JPA (Hibernate 6) |
| Database | Oracle Database (ojdbc11) |
| 실시간 통신 | Server-Sent Events (SseEmitter) |
| 캐싱 | Spring Cache (ConcurrentMapCacheManager) |
| 빌드 | Gradle 8.x |
| IDE | Eclipse STS 4 |

---

## 3. API 엔드포인트

### SSE — 실시간 스트리밍
| Method | URL | 설명 |
|---|---|---|
| GET | `/bike/connect` | SSE 연결 수립. 10초마다 `userCount` 이벤트 수신 |

**이벤트 스펙**
```
event: connect
data: SSE connected

event: userCount
data: 3
```

### 차트 데이터 — 단일 엔드포인트 동적 라우팅
| Method | URL | 설명 |
|---|---|---|
| GET | `/bike/stats/top-stations` | 상위 10개 대여소 (USE_COUNT 합계 내림차순) |
| GET | `/bike/stats/demographics` | 인구통계 (AGE_GROUP · GENDER별 집계) |
| GET | `/bike/stats/daily-trend`  | 일별 대여 추이 (RENT_DATE 오름차순) |

**응답 예시 — top-stations**
```json
[
  { "stationName": "강남역 1번 출구", "totalCount": 1540 },
  { "stationName": "홍대입구역",      "totalCount": 1230 }
]
```

### 지도 데이터
| Method | URL | 설명 |
|---|---|---|
| GET | `/bike/map` | 스테이션 목록 (로컬 캐시 적용) |

---

## 4. 프로젝트 구조

```
src/main/java/com/dashboard/app/
├── AppApplication.java              # @SpringBootApplication
│                                    # @EnableScheduling @EnableCaching
└── bike/
    ├── controller/
    │   └── BikeController.java      # REST + SSE 진입점
    ├── service/
    │   └── BikeService.java         # SSE 생명주기 · 차트 라우팅 · 캐시
    ├── repository/
    │   └── BikeRepository.java      # JpaRepository + Native Query 3종
    ├── domain/
    │   └── Bike.java                # @Entity — BIKE 테이블 매핑
    └── dto/
        ├── TopStationDto.java       # Projection 인터페이스
        ├── DemographicsDto.java     # Projection 인터페이스
        ├── DailyTrendDto.java       # Projection 인터페이스
        └── BikeMapDto.java          # 지도 응답 DTO
```

---

## 5. 로컬 실행 방법

### 사전 조건
- JDK 17
- Oracle XE (localhost:1521, sid: xe, user: hr)
- Eclipse STS 4

### IDE 실행 (권장)
```
1. Eclipse에서 F5 (Refresh)
2. AppApplication.launch 더블클릭 → Run
```

### CLI 실행
```bash
./gradlew bootRun
```

### IDE 메타데이터 초기화
```bash
./gradlew cleanEclipse eclipse
```

---

## 6. Architecture Decision Records

> 상세 내용: [`docs/ADR-001-bike-dashboard.md`](docs/ADR-001-bike-dashboard.md)

| ADR | 제목 | 결정 |
|---|---|---|
| ADR-001-1 | Java 버전 관리 전략 | `sourceCompatibility` 고정 |
| ADR-001-2 | IDE 런타임 에러 우회 | `.launch` 정적 파일 |
| ADR-001-3 | SSE 스레드 안전성 | `CopyOnWriteArrayList` + 생명주기 콜백 |
| ADR-001-4 | DB 통계 쿼리 전략 | Native Query + Projection 인터페이스 |
| ADR-001-5 | 성능 최적화 계층 | `@Cacheable` + `@Scheduled` |
