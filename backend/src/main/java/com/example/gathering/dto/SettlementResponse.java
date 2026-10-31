package com.example.gathering.dto;

import java.util.List;

public record SettlementResponse(List<BalanceResponse> balances, List<TransferResponse> transfers) {
}
