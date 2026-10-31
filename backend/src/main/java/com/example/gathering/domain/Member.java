package com.example.gathering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 모임(채팅방) 참여자. 별명은 겹칠 수 있으므로 id로 구분한다.
 */
@Entity
@Table(name = "gathering_member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @Column(nullable = false, length = 50)
    private String nickname;

    protected Member() {
    }

    public Member(Gathering gathering, String nickname) {
        this.gathering = gathering;
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public Gathering getGathering() {
        return gathering;
    }

    public String getNickname() {
        return nickname;
    }
}
