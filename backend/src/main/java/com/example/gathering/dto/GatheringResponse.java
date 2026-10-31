package com.example.gathering.dto;

import java.util.List;

public record GatheringResponse(Long id, String name, List<MemberResponse> members) {
}
