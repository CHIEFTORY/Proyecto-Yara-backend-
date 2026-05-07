package com.yara.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // 🔥 buckets por email
    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    // =========================
    // CREAR BUCKET
    // =========================

    private Bucket createNewBucket() {

        Bandwidth limit = Bandwidth.classic(
                5, // 🔥 máximo 5 intentos
                Refill.greedy(
                        5,
                        Duration.ofMinutes(1)
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // =========================
    // VALIDAR INTENTO
    // =========================

    public boolean allowAttempt(String email) {

        Bucket bucket = buckets.computeIfAbsent(
                email,
                k -> createNewBucket()
        );

        return bucket.tryConsume(1);
    }
}