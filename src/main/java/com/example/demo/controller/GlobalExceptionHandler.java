package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 전역 예외 핸들러
 * - 클라이언트 오류(4xx)를 올바른 HTTP 상태로 반환
 * - 내부 예외 메시지가 500으로 그대로 노출되는 것을 방지
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 잘못된 파라미터 (status 열거형 오류, 범위 초과 등) → 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    /** 존재하지 않는 리소스 → 404 */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
}
