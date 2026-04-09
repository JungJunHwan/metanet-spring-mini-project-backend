package com.dashboard.app.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * Task 2: List<Map<String, Object>> 같은 제네릭 타입을 Redis에 안전하게
     * 직렬화/역직렬화하기 위한 커스텀 ObjectMapper.
     *
     * [핵심 설정 설명]
     * 1. PolymorphicTypeValidator
     *    - activateDefaultTyping() 호출 시 허용할 클래스 패키지를 명시적으로 화이트리스트 처리.
     *    - java.util.* (ArrayList, HashMap 등)과 프로젝트 패키지만 허용 →
     *      역직렬화 과정의 보안 취약점(Deserialization Gadget Attack) 방어.
     *
     * 2. activateDefaultTyping(..., NON_FINAL, PROPERTY)
     *    - NON_FINAL: String/int 같은 final 타입은 제외하고, List·Map·Object 등
     *      런타임 타입 판별이 필요한 경우에만 @class 메타데이터를 삽입.
     *    - PROPERTY: @class 정보를 JSON 프로퍼티 필드로 저장
     *      → START_OBJECT 파싱 에러 방지.
     *
     * 3. JavaTimeModule + WRITE_DATES_AS_TIMESTAMPS 비활성화
     *    - LocalDate, LocalDateTime 등을 ISO-8601 문자열로 직렬화 (타임스탬프 숫자 X).
     */
    @Bean
    public ObjectMapper redisCacheObjectMapper() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util")           // ArrayList, HashMap 등 허용
                .allowIfSubType("java.lang")           // String, Long 등 허용
                .allowIfSubType("com.dashboard.app")   // 프로젝트 내부 DTO 허용
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY             // @class 를 JSON property로 삽입
        );
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                          ObjectMapper redisCacheObjectMapper) {
        // 커스텀 ObjectMapper를 주입한 Serializer
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisCacheObjectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))    // TTL: 10분 (기존 1분 → 통계 데이터 특성상 상향)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}
