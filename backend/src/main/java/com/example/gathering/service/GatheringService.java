package com.example.gathering.service;

import com.example.gathering.domain.Gathering;
import com.example.gathering.domain.Member;
import com.example.gathering.dto.AddMemberRequest;
import com.example.gathering.dto.CreateGatheringRequest;
import com.example.gathering.dto.GatheringResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.exception.ConflictException;
import com.example.gathering.exception.NotFoundException;
import com.example.gathering.repository.ExpenseParticipantRepository;
import com.example.gathering.repository.ExpenseRepository;
import com.example.gathering.repository.GatheringRepository;
import com.example.gathering.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;

    public GatheringService(GatheringRepository gatheringRepository,
                            MemberRepository memberRepository,
                            ExpenseRepository expenseRepository,
                            ExpenseParticipantRepository expenseParticipantRepository) {
        this.gatheringRepository = gatheringRepository;
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
        this.expenseParticipantRepository = expenseParticipantRepository;
    }

    @Transactional
    public GatheringResponse create(CreateGatheringRequest request) {
        Gathering gathering = gatheringRepository.save(new Gathering(request.name()));
        List<Member> members = memberRepository.saveAll(
                request.nicknames().stream().map(nickname -> new Member(gathering, nickname)).toList());
        return toResponse(gathering, members);
    }

    public GatheringResponse get(Long gatheringId) {
        Gathering gathering = findGathering(gatheringId);
        return toResponse(gathering, memberRepository.findAllByGatheringIdOrderByIdAsc(gatheringId));
    }

    @Transactional
    public MemberResponse addMember(Long gatheringId, AddMemberRequest request) {
        Gathering gathering = findGathering(gatheringId);
        return MemberResponse.from(memberRepository.save(new Member(gathering, request.nickname())));
    }

    @Transactional
    public void deleteMember(Long gatheringId, Long memberId) {
        Member member = memberRepository.findByIdAndGatheringId(memberId, gatheringId)
                .orElseThrow(() -> new NotFoundException("멤버를 찾을 수 없습니다. id=" + memberId));
        if (expenseRepository.existsByPayerId(memberId) || expenseParticipantRepository.existsByMemberId(memberId)) {
            throw new ConflictException("지출에 참여한 멤버는 삭제할 수 없습니다. id=" + memberId);
        }
        memberRepository.delete(member);
    }

    private Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new NotFoundException("모임을 찾을 수 없습니다. id=" + gatheringId));
    }

    private GatheringResponse toResponse(Gathering gathering, List<Member> members) {
        return new GatheringResponse(gathering.getId(), gathering.getName(),
                members.stream().map(MemberResponse::from).toList());
    }
}
