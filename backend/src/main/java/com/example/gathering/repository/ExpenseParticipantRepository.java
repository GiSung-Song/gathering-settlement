package com.example.gathering.repository;

import com.example.gathering.domain.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {

    boolean existsByMemberId(Long memberId);
}
