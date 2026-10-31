package com.example.gathering.settlement;

/**
 * 정산 결과 송금. from이 to에게 amount를 보낸다.
 */
public record Transfer(String from, String to, long amount) {
}
