package com.example.gathering.service;

import com.example.gathering.domain.Expense;
import com.example.gathering.domain.ExpenseParticipant;
import com.example.gathering.domain.Member;
import com.example.gathering.dto.BalanceResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.dto.SettlementResponse;
import com.example.gathering.dto.TransferResponse;
import com.example.gathering.exception.NotFoundException;
import com.example.gathering.repository.ExpenseRepository;
import com.example.gathering.repository.GatheringRepository;
import com.example.gathering.repository.MemberRepository;
import com.example.gathering.settlement.SettlementCalculator;
import com.example.gathering.settlement.Transfer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SettlementService {

    private final GatheringRepository gatheringRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementCalculator calculator = new SettlementCalculator();

    public SettlementService(GatheringRepository gatheringRepository,
                             MemberRepository memberRepository,
                             ExpenseRepository expenseRepository) {
        this.gatheringRepository = gatheringRepository;
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
    }

    public SettlementResponse settle(Long gatheringId) {
        if (!gatheringRepository.existsById(gatheringId)) {
            throw new NotFoundException("모임을 찾을 수 없습니다. id=" + gatheringId);
        }
        Map<Long, MemberResponse> members = new LinkedHashMap<>();
        for (Member member : memberRepository.findAllByGatheringIdOrderByIdAsc(gatheringId)) {
            members.put(member.getId(), MemberResponse.from(member));
        }
        List<com.example.gathering.settlement.Expense> expenses =
                expenseRepository.findAllByGatheringIdOrderByIdAsc(gatheringId).stream()
                        .map(SettlementService::toCalculatorInput)
                        .toList();

        Map<Long, Long> balances = calculator.calculateBalances(List.copyOf(members.keySet()), expenses);
        List<Transfer> transfers = calculator.settle(balances);

        return new SettlementResponse(
                balances.entrySet().stream()
                        .map(e -> new BalanceResponse(members.get(e.getKey()), e.getValue()))
                        .toList(),
                transfers.stream()
                        .map(t -> new TransferResponse(members.get(t.fromId()), members.get(t.toId()), t.amount()))
                        .toList());
    }

    /**
     * 참여자는 등록한 순서대로 넘긴다. 결제자가 참여자가 아니면 첫 번째 참여자가 나머지를 부담하기 때문이다.
     */
    private static com.example.gathering.settlement.Expense toCalculatorInput(Expense expense) {
        List<Long> participantIds = expense.getParticipants().stream()
                .sorted(Comparator.comparing(ExpenseParticipant::getId))
                .map(p -> p.getMember().getId())
                .toList();
        return new com.example.gathering.settlement.Expense(
                expense.getPayer().getId(), expense.getAmount(), participantIds);
    }
}
