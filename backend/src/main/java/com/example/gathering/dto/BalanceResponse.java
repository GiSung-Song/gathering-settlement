package com.example.gathering.dto;

/**
 * 멤버의 잔액. +면 받을 돈, -면 낼 돈.
 */
public record BalanceResponse(MemberResponse member, long balance) {
}
