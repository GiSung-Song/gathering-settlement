package com.example.gathering.repository;

import com.example.gathering.TestcontainersConfiguration;
import com.example.gathering.domain.Expense;
import com.example.gathering.domain.Gathering;
import com.example.gathering.domain.Member;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ExpenseRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    @DisplayName("모임의 지출만 등록 순서대로 조회한다")
    void findOnlyGatheringExpensesInOrder() {
        Gathering mine = em.persist(new Gathering("내 모임"));
        Gathering other = em.persist(new Gathering("남의 모임"));
        Member a = em.persist(new Member(mine, "A"));
        Member x = em.persist(new Member(other, "X"));
        Expense first = em.persist(new Expense(mine, a, 10_000, "커피", List.of(a)));
        em.persist(new Expense(other, x, 20_000, "남의 지출", List.of(x)));
        Expense second = em.persist(new Expense(mine, a, 30_000, "밥", List.of(a)));
        em.flush();
        em.clear();

        List<Expense> expenses = expenseRepository.findAllByGatheringIdOrderByIdAsc(mine.getId());

        assertThat(expenses).extracting(Expense::getId).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("지출 목록과 결제자·참여자를 쿼리 1번으로 조회한다 (N+1 없음)")
    void findWithPayerAndParticipantsInOneQuery() {
        Gathering gathering = em.persist(new Gathering("여행"));
        Member a = em.persist(new Member(gathering, "A"));
        Member b = em.persist(new Member(gathering, "B"));
        Member c = em.persist(new Member(gathering, "C"));
        em.persist(new Expense(gathering, a, 240_000, "숙소", List.of(a, b, c)));
        em.persist(new Expense(gathering, b, 80_000, "저녁", List.of(a, b, c)));
        em.persist(new Expense(gathering, c, 60_000, "술", List.of(a, b)));
        em.flush();
        em.clear();

        Statistics statistics = em.getEntityManager().getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<Expense> expenses = expenseRepository.findAllByGatheringIdOrderByIdAsc(gathering.getId());
        List<String> loaded = expenses.stream()
                .flatMap(e -> {
                    String payer = e.getPayer().getNickname();
                    return e.getParticipants().stream().map(p -> payer + "->" + p.getMember().getNickname());
                })
                .toList();

        assertThat(loaded).hasSize(8);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
