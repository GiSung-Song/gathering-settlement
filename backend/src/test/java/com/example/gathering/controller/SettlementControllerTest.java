package com.example.gathering.controller;

import com.example.gathering.dto.BalanceResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.dto.SettlementResponse;
import com.example.gathering.dto.TransferResponse;
import com.example.gathering.exception.NotFoundException;
import com.example.gathering.service.SettlementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SettlementService settlementService;

    @Test
    @DisplayName("정산 결과: 200, 잔액과 송금 목록")
    void settle() throws Exception {
        MemberResponse a = new MemberResponse(1L, "A");
        MemberResponse b = new MemberResponse(2L, "B");
        given(settlementService.settle(1L)).willReturn(new SettlementResponse(
                List.of(new BalanceResponse(a, 10_000), new BalanceResponse(b, -10_000)),
                List.of(new TransferResponse(b, a, 10_000))));

        mvc.perform(get("/api/gatherings/1/settlement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances[0].member.nickname").value("A"))
                .andExpect(jsonPath("$.balances[0].balance").value(10_000))
                .andExpect(jsonPath("$.balances[1].balance").value(-10_000))
                .andExpect(jsonPath("$.transfers[0].from.id").value(2))
                .andExpect(jsonPath("$.transfers[0].to.id").value(1))
                .andExpect(jsonPath("$.transfers[0].amount").value(10_000));
    }

    @Test
    @DisplayName("정산 결과: 모임이 없으면 404")
    void settleNotFound() throws Exception {
        given(settlementService.settle(1L)).willThrow(new NotFoundException("모임을 찾을 수 없습니다."));

        mvc.perform(get("/api/gatherings/1/settlement"))
                .andExpect(status().isNotFound());
    }
}
