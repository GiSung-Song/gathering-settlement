package com.example.gathering.dto;

public record TransferResponse(MemberResponse from, MemberResponse to, long amount) {
}
