package com.example.gathering.settlement;

import java.util.List;

/**
 * 한 번의 결제. 금액을 participantIds에게 균등 분배한다.
 */
public record Expense(Long payerId, long amount, List<Long> participantIds) {
}
