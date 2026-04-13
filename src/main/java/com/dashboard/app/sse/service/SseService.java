package com.dashboard.app.sse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(600_000L);
        emitters.add(emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE connected"));
        } catch (IOException e) {
            emitters.remove(emitter);
            return emitter;
        }

        emitter.onTimeout(()    -> { emitters.remove(emitter); broadcastUserCount(); });
        emitter.onCompletion(() -> { emitters.remove(emitter); broadcastUserCount(); });
        emitter.onError(e       -> { emitters.remove(emitter); broadcastUserCount(); });

        // 즉시 전역 브로드캐스트 (새로운 접속 발생)
        broadcastUserCount();

        return emitter;
    }

    @Scheduled(fixedRate = 10_000)
    public void broadcastUserCount() {
        int count = emitters.size();
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("userCount")
                        .data(count));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
        log.debug("SSE broadcast: userCount={}, dead={}", count, deadEmitters.size());
    }
}
