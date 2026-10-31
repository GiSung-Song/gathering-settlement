package com.example.gathering.service;

import com.example.gathering.TestcontainersConfiguration;
import com.example.gathering.dto.CreateExpenseRequest;
import com.example.gathering.dto.CreateGatheringRequest;
import com.example.gathering.dto.ExpenseResponse;
import com.example.gathering.dto.GatheringResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.exception.BadRequestException;
import com.example.gathering.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class ExpenseServiceTest {

    private static final long NOT_EXISTING_ID = 999_999L;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private GatheringService gatheringService;

    private Long gatheringId;
    private Long a;
    private Long b;
    private Long c;
    private Long otherGatheringMember;

    @BeforeEach
    void setUp() {
        GatheringResponse gathering = gatheringService.create(
                new CreateGatheringRequest("여행", List.of("A", "B", "C")));
        gatheringId = gathering.id();
        a = gathering.members().get(0).id();
        b = gathering.members().get(1).id();
        c = gathering.members().get(2).id();
        otherGatheringMember = gatheringService.create(
                new CreateGatheringRequest("남의 모임", List.of("X"))).members().getFirst().id();
    }

    @Test
    @DisplayName("지출을 등록하면 결제자와 참여자 별명이 함께 반환된다")
    void create() {
        ExpenseResponse created = expenseService.create(gatheringId,
                new CreateExpenseRequest(a, 60_000, "술", List.of(a, b)));

        assertThat(created.id()).isNotNull();
        assertThat(created.payer()).isEqualTo(new MemberResponse(a, "A"));
        assertThat(created.amount()).isEqualTo(60_000);
        assertThat(created.description()).isEqualTo("술");
        assertThat(created.participants()).containsExactly(new MemberResponse(a, "A"), new MemberResponse(b, "B"));
        assertThat(created.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("결제자가 참여자에 없어도 등록할 수 있다")
    void createWhenPayerNotParticipating() {
        ExpenseResponse created = expenseService.create(gatheringId,
                new CreateExpenseRequest(c, 30_000, "대신 결제", List.of(a, b)));

        assertThat(created.payer().id()).isEqualTo(c);
        assertThat(created.participants()).extracting(MemberResponse::id).containsExactly(a, b);
    }

    @Test
    @DisplayName("없는 모임에 지출을 등록하면 NotFoundException")
    void createInNotExistingGathering() {
        assertThatThrownBy(() -> expenseService.create(NOT_EXISTING_ID,
                new CreateExpenseRequest(a, 10_000, "커피", List.of(a))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("결제자가 이 모임의 멤버가 아니면 BadRequestException")
    void createWithPayerFromOtherGathering() {
        assertThatThrownBy(() -> expenseService.create(gatheringId,
                new CreateExpenseRequest(otherGatheringMember, 10_000, "커피", List.of(a))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("참여자 중 이 모임의 멤버가 아닌 사람이 있으면 BadRequestException")
    void createWithParticipantFromOtherGathering() {
        assertThatThrownBy(() -> expenseService.create(gatheringId,
                new CreateExpenseRequest(a, 10_000, "커피", List.of(a, otherGatheringMember))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("참여자가 중복되면 BadRequestException")
    void createWithDuplicatedParticipants() {
        assertThatThrownBy(() -> expenseService.create(gatheringId,
                new CreateExpenseRequest(a, 10_000, "커피", List.of(a, b, a))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("지출 목록을 등록 순서대로 조회한다")
    void list() {
        ExpenseResponse first = expenseService.create(gatheringId,
                new CreateExpenseRequest(a, 240_000, "숙소", List.of(a, b, c)));
        ExpenseResponse second = expenseService.create(gatheringId,
                new CreateExpenseRequest(b, 80_000, "저녁", List.of(a, b, c)));

        assertThat(expenseService.list(gatheringId)).containsExactly(first, second);
    }

    @Test
    @DisplayName("없는 모임의 지출 목록을 조회하면 NotFoundException")
    void listOfNotExistingGathering() {
        assertThatThrownBy(() -> expenseService.list(NOT_EXISTING_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("지출을 삭제한다")
    void delete() {
        ExpenseResponse created = expenseService.create(gatheringId,
                new CreateExpenseRequest(a, 10_000, "커피", List.of(a)));

        expenseService.delete(gatheringId, created.id());

        assertThat(expenseService.list(gatheringId)).isEmpty();
    }

    @Test
    @DisplayName("다른 모임의 지출을 삭제하려 하면 NotFoundException")
    void deleteExpenseOfOtherGathering() {
        ExpenseResponse created = expenseService.create(gatheringId,
                new CreateExpenseRequest(a, 10_000, "커피", List.of(a)));
        Long otherGatheringId = gatheringService.create(new CreateGatheringRequest("남의 모임", List.of())).id();

        assertThatThrownBy(() -> expenseService.delete(otherGatheringId, created.id()))
                .isInstanceOf(NotFoundException.class);
    }
}
