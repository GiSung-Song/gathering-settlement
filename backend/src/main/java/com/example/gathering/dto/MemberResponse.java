package com.example.gathering.dto;

import com.example.gathering.domain.Member;

public record MemberResponse(Long id, String nickname) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getNickname());
    }
}
