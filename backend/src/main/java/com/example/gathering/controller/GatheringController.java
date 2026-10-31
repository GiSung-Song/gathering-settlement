package com.example.gathering.controller;

import com.example.gathering.dto.AddMemberRequest;
import com.example.gathering.dto.CreateGatheringRequest;
import com.example.gathering.dto.GatheringResponse;
import com.example.gathering.dto.MemberResponse;
import com.example.gathering.service.GatheringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/gatherings")
public class GatheringController {

    private final GatheringService gatheringService;

    public GatheringController(GatheringService gatheringService) {
        this.gatheringService = gatheringService;
    }

    @PostMapping
    public ResponseEntity<GatheringResponse> create(@Valid @RequestBody CreateGatheringRequest request) {
        GatheringResponse response = gatheringService.create(request);
        return ResponseEntity.created(URI.create("/api/gatherings/" + response.id())).body(response);
    }

    @GetMapping("/{gatheringId}")
    public GatheringResponse get(@PathVariable Long gatheringId) {
        return gatheringService.get(gatheringId);
    }

    @PostMapping("/{gatheringId}/members")
    public ResponseEntity<MemberResponse> addMember(@PathVariable Long gatheringId,
                                                    @Valid @RequestBody AddMemberRequest request) {
        MemberResponse response = gatheringService.addMember(gatheringId, request);
        return ResponseEntity.created(
                URI.create("/api/gatherings/" + gatheringId + "/members/" + response.id())).body(response);
    }

    @DeleteMapping("/{gatheringId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long gatheringId, @PathVariable Long memberId) {
        gatheringService.deleteMember(gatheringId, memberId);
        return ResponseEntity.noContent().build();
    }
}
