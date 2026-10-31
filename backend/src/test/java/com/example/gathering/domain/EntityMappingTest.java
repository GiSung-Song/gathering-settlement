package com.example.gathering.domain;

import com.example.gathering.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class EntityMappingTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("모임과 멤버를 저장한다. 같은 모임에 별명이 같은 멤버도 허용")
    void saveGatheringAndMembers() {
        Gathering gathering = em.persist(new Gathering("제주도 여행"));
        Member first = em.persist(new Member(gathering, "민수"));
        Member second = em.persist(new Member(gathering, "민수"));
        em.flush();
        em.clear();

        Member found = em.find(Member.class, second.getId());
        assertThat(found.getNickname()).isEqualTo("민수");
        assertThat(found.getGathering().getId()).isEqualTo(gathering.getId());
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    @DisplayName("지출을 저장하면 참여자도 함께 저장된다")
    void saveExpenseWithParticipants() {
        Gathering gathering = em.persist(new Gathering("회식"));
        Member a = em.persist(new Member(gathering, "A"));
        Member b = em.persist(new Member(gathering, "B"));
        Member c = em.persist(new Member(gathering, "C"));

        Expense expense = em.persist(new Expense(gathering, a, 60_000, "술", List.of(a, b)));
        em.flush();
        em.clear();

        Expense found = em.find(Expense.class, expense.getId());
        assertThat(found.getAmount()).isEqualTo(60_000);
        assertThat(found.getDescription()).isEqualTo("술");
        assertThat(found.getPayer().getId()).isEqualTo(a.getId());
        assertThat(found.getParticipants())
                .extracting(p -> p.getMember().getId())
                .containsExactlyInAnyOrder(a.getId(), b.getId())
                .doesNotContain(c.getId());
    }

    @Test
    @DisplayName("지출을 삭제하면 참여자도 함께 삭제된다")
    void deleteExpenseRemovesParticipants() {
        Gathering gathering = em.persist(new Gathering("회식"));
        Member a = em.persist(new Member(gathering, "A"));
        Expense expense = em.persist(new Expense(gathering, a, 10_000, "커피", List.of(a)));
        em.flush();

        em.remove(expense);
        em.flush();

        Long count = em.getEntityManager()
                .createQuery("select count(p) from ExpenseParticipant p", Long.class)
                .getSingleResult();
        assertThat(count).isZero();
    }
}
