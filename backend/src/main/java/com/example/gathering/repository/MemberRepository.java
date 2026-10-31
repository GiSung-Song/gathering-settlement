package com.example.gathering.repository;

import com.example.gathering.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findAllByGatheringIdOrderByIdAsc(Long gatheringId);

    Optional<Member> findByIdAndGatheringId(Long id, Long gatheringId);
}
