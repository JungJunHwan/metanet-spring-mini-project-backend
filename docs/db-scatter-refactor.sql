-- ============================================================
-- 산점도·박스플롯 리팩터링을 위한 DB 변경 스크립트
-- 실행 순서: STEP 1 → STEP 2 → STEP 3
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- STEP 1. AGG_DISTANCE_CARBON_STAT 재설계
--         (CARBON_AMOUNT 제거 → USE_TIME 추가)
--         용도: 이동거리 vs 이용시간 산점도
-- ────────────────────────────────────────────────────────────
DROP TABLE AGG_DISTANCE_CARBON_STAT;

CREATE TABLE AGG_DISTANCE_CARBON_STAT (
    ID              NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    DISTRICT        VARCHAR2(50),
    RENT_MONTH      NUMBER(2),
    DISTANCE        NUMBER,        -- 이동거리 (m)
    USE_TIME        NUMBER,        -- 이용시간 (분)
    TOTAL_USE_COUNT NUMBER         -- 동일 (거리, 시간) 이용 건수
);

INSERT INTO AGG_DISTANCE_CARBON_STAT (DISTRICT, RENT_MONTH, DISTANCE, USE_TIME, TOTAL_USE_COUNT)
SELECT
    S.DISTRICT,
    EXTRACT(MONTH FROM B.RENT_DATE)  AS RENT_MONTH,
    B.DISTANCE,
    B.USE_TIME,
    COUNT(*)                         AS TOTAL_USE_COUNT
FROM BIKE B
JOIN STATION S ON B.STATION_ID = S.STATION_ID
WHERE B.DISTANCE IS NOT NULL
  AND B.USE_TIME  IS NOT NULL
  AND B.DISTANCE  > 0
  AND B.USE_TIME  > 0
GROUP BY S.DISTRICT, EXTRACT(MONTH FROM B.RENT_DATE), B.DISTANCE, B.USE_TIME;

COMMIT;

-- ────────────────────────────────────────────────────────────
-- STEP 2. AGG_STATION_STAT 에 AVG_DISTANCE 컬럼 추가
--         용도: 대여소별 이용건수 vs 평균거리 산점도
-- ────────────────────────────────────────────────────────────
ALTER TABLE AGG_STATION_STAT ADD AVG_DISTANCE NUMBER;

UPDATE AGG_STATION_STAT agg
SET AVG_DISTANCE = (
    SELECT AVG(B.DISTANCE)
    FROM   BIKE B
    JOIN   STATION S ON B.STATION_ID = S.STATION_ID
    WHERE  S.DISTRICT   = agg.DISTRICT
      AND  EXTRACT(MONTH FROM B.RENT_DATE) = agg.RENT_MONTH
      AND  B.STATION_ID = agg.STATION_ID
      AND  B.DISTANCE IS NOT NULL
      AND  B.DISTANCE > 0
);

COMMIT;

-- ────────────────────────────────────────────────────────────
-- STEP 3. AGG_AGE_DISTANCE_STAT 신규 생성
--         용도: 연령대별 이동거리 분포 박스플롯
-- ────────────────────────────────────────────────────────────
CREATE TABLE AGG_AGE_DISTANCE_STAT (
    ID              NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    DISTRICT        VARCHAR2(50),
    RENT_MONTH      NUMBER(2),
    AGE_GROUP       VARCHAR2(20),
    MIN_DIST        NUMBER,        -- 최솟값
    Q1_DIST         NUMBER,        -- 25백분위수
    MEDIAN_DIST     NUMBER,        -- 중앙값 (50백분위수)
    Q3_DIST         NUMBER,        -- 75백분위수
    MAX_DIST        NUMBER,        -- 최댓값
    TOTAL_USE_COUNT NUMBER         -- 해당 그룹 이용 건수
);

INSERT INTO AGG_AGE_DISTANCE_STAT
    (DISTRICT, RENT_MONTH, AGE_GROUP,
     MIN_DIST, Q1_DIST, MEDIAN_DIST, Q3_DIST, MAX_DIST, TOTAL_USE_COUNT)
SELECT
    S.DISTRICT,
    EXTRACT(MONTH FROM B.RENT_DATE)                                          AS RENT_MONTH,
    B.AGE_GROUP,
    MIN(B.DISTANCE)                                                           AS MIN_DIST,
    PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY B.DISTANCE)                 AS Q1_DIST,
    PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY B.DISTANCE)                 AS MEDIAN_DIST,
    PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY B.DISTANCE)                 AS Q3_DIST,
    MAX(B.DISTANCE)                                                           AS MAX_DIST,
    COUNT(*)                                                                  AS TOTAL_USE_COUNT
FROM BIKE B
JOIN STATION S ON B.STATION_ID = S.STATION_ID
WHERE B.DISTANCE  IS NOT NULL
  AND B.AGE_GROUP IS NOT NULL
  AND B.DISTANCE  > 0
GROUP BY S.DISTRICT, EXTRACT(MONTH FROM B.RENT_DATE), B.AGE_GROUP;

COMMIT;

