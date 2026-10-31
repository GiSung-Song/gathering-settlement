package com.example.gathering.settlement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettlementCalculator {

    /**
     * 멤버별 잔액(낸 돈 - 쓴 돈)을 계산한다.
     * 균등 분배 후 1원 단위 나머지는 결제자가 부담하고,
     * 결제자가 참여자가 아니면 참여자 중 첫 번째가 부담한다.
     */
    public Map<Long, Long> calculateBalances(List<Long> memberIds, List<Expense> expenses) {
        Map<Long, Long> balances = new LinkedHashMap<>();
        for (Long memberId : memberIds) {
            balances.put(memberId, 0L);
        }

        for (Expense expense : expenses) {
            List<Long> participants = expense.participantIds();
            long share = expense.amount() / participants.size();
            long remainder = expense.amount() % participants.size();
            Long remainderBearer = participants.contains(expense.payerId())
                    ? expense.payerId()
                    : participants.getFirst();

            balances.merge(expense.payerId(), expense.amount(), Long::sum);
            for (Long participant : participants) {
                balances.merge(participant, -share, Long::sum);
            }
            balances.merge(remainderBearer, -remainder, Long::sum);
        }
        return balances;
    }

    /**
     * 잔액이 가장 큰 채권자와 가장 큰 채무자를 반복 매칭해 송금 목록을 만든다.
     * 금액이 같으면 먼저 등록된 멤버를 우선한다.
     */
    public List<Transfer> settle(Map<Long, Long> balances) {
        Map<Long, Long> remaining = new LinkedHashMap<>(balances);
        List<Transfer> transfers = new ArrayList<>();

        while (true) {
            Long creditor = null;
            Long debtor = null;
            for (Map.Entry<Long, Long> entry : remaining.entrySet()) {
                long balance = entry.getValue();
                if (balance > 0 && (creditor == null || balance > remaining.get(creditor))) {
                    creditor = entry.getKey();
                }
                if (balance < 0 && (debtor == null || balance < remaining.get(debtor))) {
                    debtor = entry.getKey();
                }
            }
            if (creditor == null || debtor == null) {
                return transfers;
            }

            long amount = Math.min(remaining.get(creditor), -remaining.get(debtor));
            transfers.add(new Transfer(debtor, creditor, amount));
            remaining.merge(creditor, -amount, Long::sum);
            remaining.merge(debtor, amount, Long::sum);
        }
    }

    /**
     * 완료된 송금을 잔액에 반영한다. 보낸 사람은 잔액이 늘고, 받은 사람은 줄어든다.
     */
    public Map<Long, Long> applyTransfers(Map<Long, Long> balances, List<Transfer> completed) {
        Map<Long, Long> result = new LinkedHashMap<>(balances);
        for (Transfer transfer : completed) {
            result.merge(transfer.fromId(), transfer.amount(), Long::sum);
            result.merge(transfer.toId(), -transfer.amount(), Long::sum);
        }
        return result;
    }
}
