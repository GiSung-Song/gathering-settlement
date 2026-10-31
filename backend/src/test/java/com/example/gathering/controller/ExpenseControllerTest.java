package com.example.gathering.controller;

import com.example.gathering.dto.CreateExpenseRequest;
import com.example.gathering.dto.ExpenseResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.exception.BadRequestException;
import com.example.gathering.exception.NotFoundException;
import com.example.gathering.service.ExpenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ExpenseService expenseService;

    private static final ExpenseResponse SAMPLE = new ExpenseResponse(100L,
            new MemberResponse(10L, "A"), 60_000, "술",
            List.of(new MemberResponse(10L, "A"), new MemberResponse(11L, "B")),
            LocalDateTime.of(2026, 10, 31, 12, 0));

    @Test
    @DisplayName("지출 등록: 201과 Location, 지출 정보를 반환")
    void create() throws Exception {
        given(expenseService.create(1L, new CreateExpenseRequest(10L, 60_000, "술", List.of(10L, 11L))))
                .willReturn(SAMPLE);

        mvc.perform(post("/api/gatherings/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payerId": 10, "amount": 60000, "description": "술", "participantIds": [10, 11]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/gatherings/1/expenses/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.payer.nickname").value("A"))
                .andExpect(jsonPath("$.amount").value(60_000))
                .andExpect(jsonPath("$.description").value("술"))
                .andExpect(jsonPath("$.participants[1].id").value(11))
                .andExpect(jsonPath("$.createdAt").value("2026-10-31T12:00:00"));
    }

    @Test
    @DisplayName("지출 등록: 금액이 0 이하이면 400")
    void createWithNonPositiveAmount() throws Exception {
        mvc.perform(post("/api/gatherings/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payerId": 10, "amount": 0, "description": "술", "participantIds": [10]}
                                """))
                .andExpect(status().isBadRequest());

        verify(expenseService, never()).create(anyLong(), any());
    }

    @Test
    @DisplayName("지출 등록: 참여자가 없으면 400")
    void createWithoutParticipants() throws Exception {
        mvc.perform(post("/api/gatherings/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payerId": 10, "amount": 1000, "description": "술", "participantIds": []}
                                """))
                .andExpect(status().isBadRequest());

        verify(expenseService, never()).create(anyLong(), any());
    }

    @Test
    @DisplayName("지출 등록: 내용이 비어 있으면 400")
    void createWithBlankDescription() throws Exception {
        mvc.perform(post("/api/gatherings/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payerId": 10, "amount": 1000, "description": "", "participantIds": [10]}
                                """))
                .andExpect(status().isBadRequest());

        verify(expenseService, never()).create(anyLong(), any());
    }

    @Test
    @DisplayName("지출 등록: 다른 모임의 멤버를 지정하면 400")
    void createWithMemberOfOtherGathering() throws Exception {
        given(expenseService.create(anyLong(), any()))
                .willThrow(new BadRequestException("이 모임의 멤버가 아닙니다."));

        mvc.perform(post("/api/gatherings/1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payerId": 99, "amount": 1000, "description": "술", "participantIds": [10]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("지출 목록: 200")
    void list() throws Exception {
        given(expenseService.list(1L)).willReturn(List.of(SAMPLE));

        mvc.perform(get("/api/gatherings/1/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("술"));
    }

    @Test
    @DisplayName("지출 목록: 모임이 없으면 404")
    void listNotFound() throws Exception {
        given(expenseService.list(1L)).willThrow(new NotFoundException("모임을 찾을 수 없습니다."));

        mvc.perform(get("/api/gatherings/1/expenses"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("지출 삭제: 204")
    void delete_() throws Exception {
        mvc.perform(delete("/api/gatherings/1/expenses/100"))
                .andExpect(status().isNoContent());

        verify(expenseService).delete(1L, 100L);
    }

    @Test
    @DisplayName("지출 삭제: 없으면 404")
    void deleteNotFound() throws Exception {
        willThrow(new NotFoundException("지출을 찾을 수 없습니다."))
                .given(expenseService).delete(1L, 100L);

        mvc.perform(delete("/api/gatherings/1/expenses/100"))
                .andExpect(status().isNotFound());
    }
}
