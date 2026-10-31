package com.example.gathering.settlement;

import java.util.List;

/**
 * 한 번의 결제. 금액을 participants에게 균등 분배한다.
 */
public record Expense(String payer, long amount, List<String> participants) {
}
