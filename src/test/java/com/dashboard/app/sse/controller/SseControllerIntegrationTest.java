package com.dashboard.app.sse.controller;

import com.dashboard.app.sse.service.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * SSE 통합 테스트
 * - SseEmitter는 HTTP 스트림이 닫히지 않으므로 asyncDispatch / getAsyncResult() 사용 불가.
 * - Controller 레이어: asyncStarted 여부만 검증
 * - Service 레이어: ReflectionTestUtils로 내부 emitters 목록에 mock emitter를 직접 주입하여 분기 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SseService sseService;

    /** 테스트 후 emitters 목록 초기화하여 다른 테스트에 영향을 주지 않음 */
    @AfterEach
    @SuppressWarnings("unchecked")
    void tearDown() {
        CopyOnWriteArrayList<SseEmitter> emitters =
                (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(sseService, "emitters");
        if (emitters != null) {
            // 남아있는 모든 emitter 완료 처리 후 목록 초기화
            emitters.forEach(e -> {
                try { e.complete(); } catch (Exception ignored) {}
            });
            emitters.clear();
        }
    }

    // ── 1. Controller: HTTP 레이어 검증 ──────────────────────────────
    // SSE 스트림은 종료되지 않으므로 asyncStarted 여부만 검증

    @Test
    @DisplayName("1. SSE 연결 - 비동기 요청 정상 시작 (asyncStarted == true)")
    void testSseConnect_asyncStarted() throws Exception {
        mockMvc.perform(
                get("/sse/connect")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted()) // SSE는 비동기 응답으로 시작
                .andDo(print());
    }

    @Test
    @DisplayName("2. SSE 연결 2회 - 매번 새 비동기 요청 시작")
    void testSseConnect_multipleRequests_eachAsyncStarted() throws Exception {
        mockMvc.perform(get("/sse/connect").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());

        mockMvc.perform(get("/sse/connect").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());
    }

    // ── 2. Service: 내부 emitters 목록 직접 제어로 분기 검증 ─────────

    @Test
    @DisplayName("3. broadcastUserCount() - emitters가 비어있을 때 (루프 0회 실행)")
    @SuppressWarnings("unchecked")
    void testBroadcastUserCount_emptyList() {
        CopyOnWriteArrayList<SseEmitter> emitters =
                (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(sseService, "emitters");
        assertThat(emitters).isNotNull();
        emitters.clear(); // emitters 비우기

        // 빈 목록에서 broadcast → 루프 0회, deadEmitters 없음 (예외 없이 실행)
        sseService.broadcastUserCount();
    }

    @Test
    @DisplayName("4. broadcastUserCount() - 정상 emitter에게 이벤트 전송 (Branch: IOException 미발생)")
    @SuppressWarnings("unchecked")
    void testBroadcastUserCount_withActiveEmitter() throws Exception {
        // HTTP를 통해 emitter를 SseService.emitters에 등록
        mockMvc.perform(get("/sse/connect").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());

        CopyOnWriteArrayList<SseEmitter> emitters =
                (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(sseService, "emitters");
        assertThat(emitters).isNotNull();
        assertThat(emitters).isNotEmpty(); // emitter가 등록되어 있음

        // 정상 연결된 emitter에게 broadcast → send 성공 분기
        sseService.broadcastUserCount();
    }

    @Test
    @DisplayName("5. broadcastUserCount() - 스케줄러 반복 실행 (예외 없이 실행 되어야 함)")
    void testBroadcastUserCount_scheduledNoException() {
        // @Scheduled(fixedRate = 10_000) 메서드 직접 호출 시 예외 또는 주호 없이 실행되어야 함
        // emitters가 비어있을 때도 정상 실행
        sseService.broadcastUserCount(); // 예외 발생 없으면 PASS
    }

    @Test
    @DisplayName("6. 다중 emitter broadcast - connect() 등록 후 에미터 수 컵실 확인")
    @SuppressWarnings("unchecked")
    void testBroadcastUserCount_multipleEmitters() throws Exception {
        CopyOnWriteArrayList<SseEmitter> emitters =
                (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(sseService, "emitters");
        assertThat(emitters).isNotNull();
        emitters.clear();

        // 클라이언트 2개 연결 → emitters.size() == 2
        mockMvc.perform(get("/sse/connect").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());
        mockMvc.perform(get("/sse/connect").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());

        assertThat(emitters.size()).isGreaterThanOrEqualTo(2);

        // 2개 emitter 모두에게 broadcast 문제 없이 실행되어야 함
        sseService.broadcastUserCount();
    }

    @Test
    @DisplayName("7. emitters에 등록된 개수 검증 - connect() 호출 시마다 증가")
    @SuppressWarnings("unchecked")
    void testConnect_addsEmitterToList() throws Exception {
        CopyOnWriteArrayList<SseEmitter> emitters =
                (CopyOnWriteArrayList<SseEmitter>) ReflectionTestUtils.getField(sseService, "emitters");
        assertThat(emitters).isNotNull();
        emitters.clear();

        int before = emitters.size();

        mockMvc.perform(get("/sse/connect").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted());

        // connect() 호출 후 emitters 목록에 1개 추가됨
        assertThat(emitters.size()).isGreaterThan(before);
    }
}
