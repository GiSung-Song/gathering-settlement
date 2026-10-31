package com.example.gathering.controller;

import com.example.gathering.dto.AddMemberRequest;
import com.example.gathering.dto.CreateGatheringRequest;
import com.example.gathering.dto.GatheringResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.exception.ConflictException;
import com.example.gathering.exception.NotFoundException;
import com.example.gathering.service.GatheringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

@WebMvcTest(GatheringController.class)
class GatheringControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GatheringService gatheringService;

    private static final GatheringResponse SAMPLE = new GatheringResponse(1L, "제주도 여행",
            List.of(new MemberResponse(10L, "민수"), new MemberResponse(11L, "지영")));

    @Test
    @DisplayName("모임 생성: 201과 Location, 모임·멤버를 반환")
    void create() throws Exception {
        given(gatheringService.create(new CreateGatheringRequest("제주도 여행", List.of("민수", "지영"))))
                .willReturn(SAMPLE);

        mvc.perform(post("/api/gatherings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "제주도 여행", "nicknames": ["민수", "지영"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/gatherings/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("제주도 여행"))
                .andExpect(jsonPath("$.members[0].id").value(10))
                .andExpect(jsonPath("$.members[0].nickname").value("민수"))
                .andExpect(jsonPath("$.members[1].nickname").value("지영"));
    }

    @Test
    @DisplayName("모임 생성: 이름이 비어 있으면 400")
    void createWithBlankName() throws Exception {
        mvc.perform(post("/api/gatherings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "nicknames": ["민수"]}
                                """))
                .andExpect(status().isBadRequest());

        verify(gatheringService, never()).create(any());
    }

    @Test
    @DisplayName("모임 생성: 별명 중 빈 값이 있으면 400")
    void createWithBlankNickname() throws Exception {
        mvc.perform(post("/api/gatherings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "회식", "nicknames": ["민수", ""]}
                                """))
                .andExpect(status().isBadRequest());

        verify(gatheringService, never()).create(any());
    }

    @Test
    @DisplayName("모임 조회: 200")
    void get_() throws Exception {
        given(gatheringService.get(1L)).willReturn(SAMPLE);

        mvc.perform(get("/api/gatherings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("제주도 여행"))
                .andExpect(jsonPath("$.members.length()").value(2));
    }

    @Test
    @DisplayName("모임 조회: 없으면 404")
    void getNotFound() throws Exception {
        given(gatheringService.get(1L)).willThrow(new NotFoundException("모임을 찾을 수 없습니다."));

        mvc.perform(get("/api/gatherings/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("멤버 추가: 201")
    void addMember() throws Exception {
        given(gatheringService.addMember(1L, new AddMemberRequest("철수")))
                .willReturn(new MemberResponse(12L, "철수"));

        mvc.perform(post("/api/gatherings/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "철수"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.nickname").value("철수"));
    }

    @Test
    @DisplayName("멤버 추가: 별명이 50자를 넘으면 400")
    void addMemberWithTooLongNickname() throws Exception {
        String nickname = "가".repeat(51);

        mvc.perform(post("/api/gatherings/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \"" + nickname + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("멤버 삭제: 204")
    void deleteMember() throws Exception {
        mvc.perform(delete("/api/gatherings/1/members/10"))
                .andExpect(status().isNoContent());

        verify(gatheringService).deleteMember(1L, 10L);
    }

    @Test
    @DisplayName("멤버 삭제: 지출에 참여한 멤버면 409")
    void deleteMemberInUse() throws Exception {
        willThrow(new ConflictException("지출에 참여한 멤버는 삭제할 수 없습니다."))
                .given(gatheringService).deleteMember(1L, 10L);

        mvc.perform(delete("/api/gatherings/1/members/10"))
                .andExpect(status().isConflict());
    }
}
