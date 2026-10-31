package com.example.gathering.dto;

import com.example.gathering.domain.Expense;
import com.example.gathering.domain.ExpenseParticipant;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ExpenseResponse(
        Long id,
        MemberResponse payer,
        long amount,
        String description,
        List<MemberResponse> participants,
        LocalDateTime createdAt) {

    /**
     * 참여자는 등록한 순서대로 반환한다.
     */
    public static ExpenseResponse from(Expense expense) {
        List<MemberResponse> participants = expense.getParticipants().stream()
                .sorted(Comparator.comparing(ExpenseParticipant::getId))
                .map(p -> MemberResponse.from(p.getMember()))
                .toList();
        return new ExpenseResponse(expense.getId(), MemberResponse.from(expense.getPayer()),
                expense.getAmount(), expense.getDescription(), participants, expense.getCreatedAt());
    }
}
