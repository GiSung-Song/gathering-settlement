package com.example.gathering.repository;

import com.example.gathering.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    boolean existsByPayerId(Long memberId);

    /**
     * 결제자와 참여자까지 fetch join으로 한 번에 읽는다 (N+1 방지).
     */
    @Query("""
            select distinct e from Expense e
            join fetch e.payer
            left join fetch e.participants p
            left join fetch p.member
            where e.gathering.id = :gatheringId
            order by e.id asc
            """)
    List<Expense> findAllByGatheringIdOrderByIdAsc(Long gatheringId);

    Optional<Expense> findByIdAndGatheringId(Long id, Long gatheringId);
}
