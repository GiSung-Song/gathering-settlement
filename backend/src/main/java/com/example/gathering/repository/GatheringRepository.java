package com.example.gathering.repository;

import com.example.gathering.domain.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {
}
