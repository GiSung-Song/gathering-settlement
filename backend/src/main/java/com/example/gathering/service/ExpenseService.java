package com.example.gathering.service;

import com.example.gathering.domain.Expense;
import com.example.gathering.domain.Gathering;
import com.example.gathering.domain.Member;
import com.example.gathering.dto.CreateExpenseRequest;
import com.example.gathering.dto.ExpenseResponse;
import com.example.gathering.exception.BadRequestException;
import com.example.gathering.exception.NotFoundException;
import com.example.gathering.repository.ExpenseRepository;
import com.example.gathering.repository.GatheringRepository;
import com.example.gathering.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    private final GatheringRepository gatheringRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;

    public ExpenseService(GatheringRepository gatheringRepository,
                          MemberRepository memberRepository,
                          ExpenseRepository expenseRepository) {
        this.gatheringRepository = gatheringRepository;
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse create(Long gatheringId, CreateExpenseRequest request) {
        Gathering gathering = findGathering(gatheringId);
        Map<Long, Member> members = memberRepository.findAllByGatheringIdOrderByIdAsc(gatheringId).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        Member payer = members.get(request.payerId());
        if (payer == null) {
            throw new BadRequestException("결제자가 이 모임의 멤버가 아닙니다. id=" + request.payerId());
        }
        if (new HashSet<>(request.participantIds()).size() != request.participantIds().size()) {
            throw new BadRequestException("참여자가 중복되었습니다.");
        }
        List<Member> participants = request.participantIds().stream()
                .map(id -> {
                    Member member = members.get(id);
                    if (member == null) {
                        throw new BadRequestException("참여자가 이 모임의 멤버가 아닙니다. id=" + id);
                    }
                    return member;
                })
                .toList();

        Expense expense = expenseRepository.save(
                new Expense(gathering, payer, request.amount(), request.description(), participants));
        return ExpenseResponse.from(expense);
    }

    public List<ExpenseResponse> list(Long gatheringId) {
        findGathering(gatheringId);
        return expenseRepository.findAllByGatheringIdOrderByIdAsc(gatheringId).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long gatheringId, Long expenseId) {
        Expense expense = expenseRepository.findByIdAndGatheringId(expenseId, gatheringId)
                .orElseThrow(() -> new NotFoundException("지출을 찾을 수 없습니다. id=" + expenseId));
        expenseRepository.delete(expense);
    }

    private Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new NotFoundException("모임을 찾을 수 없습니다. id=" + gatheringId));
    }
}
