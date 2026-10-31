package com.example.gathering.service;

import com.example.gathering.TestcontainersConfiguration;
import com.example.gathering.domain.Expense;
import com.example.gathering.domain.Gathering;
import com.example.gathering.domain.Member;
import com.example.gathering.dto.AddMemberRequest;
import com.example.gathering.dto.CreateGatheringRequest;
import com.example.gathering.dto.GatheringResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.exception.ConflictException;
import com.example.gathering.exception.NotFoundException;
import jakarta.persistence.EntityManager;
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
class GatheringServiceTest {

    private static final long NOT_EXISTING_ID = 999_999L;

    @Autowired
    private GatheringService gatheringService;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("모임을 만들면 멤버도 함께 만들어지고, 별명이 같아도 다른 번호를 받는다")
    void create() {
        GatheringResponse created = gatheringService.create(
                new CreateGatheringRequest("제주도 여행", List.of("민수", "지영", "민수")));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("제주도 여행");
        assertThat(created.members()).extracting(MemberResponse::nickname)
                .containsExactly("민수", "지영", "민수");
        assertThat(created.members()).extracting(MemberResponse::id)
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("모임을 조회하면 멤버가 등록 순서대로 나온다")
    void get() {
        GatheringResponse created = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("A", "B")));

        GatheringResponse found = gatheringService.get(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    @DisplayName("없는 모임을 조회하면 NotFoundException")
    void getNotFound() {
        assertThatThrownBy(() -> gatheringService.get(NOT_EXISTING_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("멤버를 추가한다")
    void addMember() {
        GatheringResponse created = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("A")));

        MemberResponse added = gatheringService.addMember(created.id(), new AddMemberRequest("B"));

        assertThat(added.nickname()).isEqualTo("B");
        assertThat(gatheringService.get(created.id()).members())
                .extracting(MemberResponse::nickname).containsExactly("A", "B");
    }

    @Test
    @DisplayName("없는 모임에 멤버를 추가하면 NotFoundException")
    void addMemberToNotExistingGathering() {
        assertThatThrownBy(() -> gatheringService.addMember(NOT_EXISTING_ID, new AddMemberRequest("B")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("지출에 참여하지 않은 멤버는 삭제할 수 있다")
    void deleteMember() {
        GatheringResponse created = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("A", "B")));
        Long memberB = created.members().get(1).id();

        gatheringService.deleteMember(created.id(), memberB);

        assertThat(gatheringService.get(created.id()).members())
                .extracting(MemberResponse::nickname).containsExactly("A");
    }

    @Test
    @DisplayName("다른 모임의 멤버를 삭제하려 하면 NotFoundException")
    void deleteMemberOfOtherGathering() {
        GatheringResponse mine = gatheringService.create(new CreateGatheringRequest("내 모임", List.of("A")));
        GatheringResponse other = gatheringService.create(new CreateGatheringRequest("남의 모임", List.of("X")));
        Long otherMember = other.members().getFirst().id();

        assertThatThrownBy(() -> gatheringService.deleteMember(mine.id(), otherMember))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("지출의 결제자인 멤버는 삭제할 수 없다")
    void deleteMemberWhoPaid() {
        GatheringResponse created = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("A", "B")));
        Member a = em.find(Member.class, created.members().get(0).id());
        Member b = em.find(Member.class, created.members().get(1).id());
        em.persist(new Expense(em.find(Gathering.class, created.id()), a, 10_000, "커피", List.of(b)));

        assertThatThrownBy(() -> gatheringService.deleteMember(created.id(), a.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("지출의 참여자인 멤버는 삭제할 수 없다")
    void deleteMemberWhoParticipated() {
        GatheringResponse created = gatheringService.create(
                new CreateGatheringRequest("회식", List.of("A", "B")));
        Member a = em.find(Member.class, created.members().get(0).id());
        Member b = em.find(Member.class, created.members().get(1).id());
        em.persist(new Expense(em.find(Gathering.class, created.id()), a, 10_000, "커피", List.of(a, b)));

        assertThatThrownBy(() -> gatheringService.deleteMember(created.id(), b.getId()))
                .isInstanceOf(ConflictException.class);
    }
}
