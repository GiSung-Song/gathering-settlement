package com.example.gathering.controller;

import com.example.gathering.dto.CreateExpenseRequest;
import com.example.gathering.dto.ExpenseResponse;
import com.example.gathering.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@PathVariable Long gatheringId,
                                                  @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseResponse response = expenseService.create(gatheringId, request);
        return ResponseEntity.created(
                URI.create("/api/gatherings/" + gatheringId + "/expenses/" + response.id())).body(response);
    }

    @GetMapping
    public List<ExpenseResponse> list(@PathVariable Long gatheringId) {
        return expenseService.list(gatheringId);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> delete(@PathVariable Long gatheringId, @PathVariable Long expenseId) {
        expenseService.delete(gatheringId, expenseId);
        return ResponseEntity.noContent().build();
    }
}
