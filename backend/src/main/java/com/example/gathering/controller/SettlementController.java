package com.example.gathering.controller;

import com.example.gathering.dto.SettlementResponse;
import com.example.gathering.service.SettlementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/api/gatherings/{gatheringId}/settlement")
    public SettlementResponse settle(@PathVariable Long gatheringId) {
        return settlementService.settle(gatheringId);
    }
}
