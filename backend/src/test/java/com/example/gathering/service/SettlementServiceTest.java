package com.example.gathering.service;

import com.example.gathering.TestcontainersConfiguration;
import com.example.gathering.dto.BalanceResponse;
import com.example.gathering.dto.CreateExpenseRequest;
import com.example.gathering.dto.CreateGatheringRequest;
import com.example.gathering.dto.GatheringResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.dto.SettlementResponse;
import com.example.gathering.dto.TransferResponse;
import com.example.gathering.exception.NotFoundException;
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
class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private ExpenseService expenseService;

    @Test
    @DisplayName("기획서 예시: 멤버별 잔액과 송금 3건")
    void sample() {
        GatheringResponse gathering = gatheringService.create(
                new CreateGatheringRequest("여행", List.of("A", "B", "C", "D")));
        MemberResponse a = gathering.members().get(0);
        MemberResponse b = gathering.members().get(1);
        MemberResponse c = gathering.members().get(2);
        MemberResponse d = gathering.members().get(3);
        List<Long> all = List.of(a.id(), b.id(), c.id(), d.id());
        expenseService.create(gathering.id(), new CreateExpenseRequest(a.id(), 240_000, "숙소", all));
        expenseService.create(gathering.id(), new CreateExpenseRequest(b.id(), 80_000, "저녁", all));
        expenseService.create(gathering.id(), new CreateExpenseRequest(c.id(), 60_000, "술",
                List.of(a.id(), b.id(), c.id())));

        SettlementResponse settlement = settlementService.settle(gathering.id());

        assertThat(settlement.balances()).containsExactly(
                new BalanceResponse(a, 140_000),
                new BalanceResponse(b, -20_000),
                new BalanceResponse(c, -40_000),
                new BalanceResponse(d, -80_000));
        assertThat(settlement.transfers()).containsExactly(
                new TransferResponse(d, a, 80_000),
                new TransferResponse(c, a, 40_000),
                new TransferResponse(b, a, 20_000));
    }

    @Test
    @DisplayName("지출이 없으면 모두 잔액 0, 송금 없음")
    void noExpenses() {
        GatheringResponse gathering = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("A", "B")));

        SettlementResponse settlement = settlementService.settle(gathering.id());

        assertThat(settlement.balances()).extracting(BalanceResponse::balance).containsExactly(0L, 0L);
        assertThat(settlement.transfers()).isEmpty();
    }

    @Test
    @DisplayName("결제자가 참여자가 아니면 입력한 참여자 순서의 첫 번째가 나머지를 부담")
    void remainderBearerFollowsInputOrder() {
        GatheringResponse gathering = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("A", "B", "C", "D")));
        MemberResponse a = gathering.members().get(0);
        MemberResponse b = gathering.members().get(1);
        MemberResponse c = gathering.members().get(2);
        MemberResponse d = gathering.members().get(3);
        expenseService.create(gathering.id(), new CreateExpenseRequest(d.id(), 100_000, "회비",
                List.of(c.id(), a.id(), b.id())));

        SettlementResponse settlement = settlementService.settle(gathering.id());

        assertThat(settlement.balances()).containsExactly(
                new BalanceResponse(a, -33_333),
                new BalanceResponse(b, -33_333),
                new BalanceResponse(c, -33_334),
                new BalanceResponse(d, 100_000));
    }

    @Test
    @DisplayName("별명이 같은 멤버도 번호로 구분해 정산한다")
    void sameNicknameMembersAreDistinguished() {
        GatheringResponse gathering = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("민수", "민수")));
        MemberResponse first = gathering.members().get(0);
        MemberResponse second = gathering.members().get(1);
        expenseService.create(gathering.id(), new CreateExpenseRequest(first.id(), 20_000, "밥",
                List.of(first.id(), second.id())));

        SettlementResponse settlement = settlementService.settle(gathering.id());

        assertThat(settlement.transfers()).containsExactly(new TransferResponse(second, first, 10_000));
    }

    @Test
    @DisplayName("없는 모임을 정산하면 NotFoundException")
    void notFound() {
        assertThatThrownBy(() -> settlementService.settle(999_999L))
                .isInstanceOf(NotFoundException.class);
    }
}
