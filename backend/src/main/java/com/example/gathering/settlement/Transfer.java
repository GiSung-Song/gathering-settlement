package com.example.gathering.settlement;

/**
 * 정산 결과 송금. fromId 멤버가 toId 멤버에게 amount를 보낸다.
 */
public record Transfer(Long fromId, Long toId, long amount) {
}
