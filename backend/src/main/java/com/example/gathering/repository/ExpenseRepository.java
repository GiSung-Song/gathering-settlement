package com.example.gathering.repository;

import com.example.gathering.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    boolean existsByPayerId(Long memberId);
}
