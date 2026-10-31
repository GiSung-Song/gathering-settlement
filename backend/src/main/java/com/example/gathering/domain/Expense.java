package com.example.gathering.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 지출. 결제자가 낸 금액을 참여자에게 균등 분배한다.
 */
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gathering_id")
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_id")
    private Member payer;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseParticipant> participants = new ArrayList<>();

    protected Expense() {
    }

    public Expense(Gathering gathering, Member payer, long amount, String description, List<Member> participants) {
        this.gathering = gathering;
        this.payer = payer;
        this.amount = amount;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        for (Member member : participants) {
            this.participants.add(new ExpenseParticipant(this, member));
        }
    }

    public Long getId() {
        return id;
    }

    public Gathering getGathering() {
        return gathering;
    }

    public Member getPayer() {
        return payer;
    }

    public long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ExpenseParticipant> getParticipants() {
        return participants;
    }
}
