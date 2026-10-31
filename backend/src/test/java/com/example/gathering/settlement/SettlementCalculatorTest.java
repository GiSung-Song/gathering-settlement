package com.example.gathering.settlement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SettlementCalculatorTest {

    private final SettlementCalculator calculator = new SettlementCalculator();

    private static final List<String> ABCD = List.of("A", "B", "C", "D");

    // 기획서 2장 예시
    private static final List<Expense> SAMPLE_EXPENSES = List.of(
            new Expense("A", 240_000, ABCD),
            new Expense("B", 80_000, ABCD),
            new Expense("C", 60_000, List.of("A", "B", "C")));

    @Test
    @DisplayName("기획서 예시: 잔액 = 낸 돈 - 쓴 돈")
    void sampleBalances() {
        Map<String, Long> balances = calculator.calculateBalances(ABCD, SAMPLE_EXPENSES);

        assertThat(balances).containsOnly(
                entry("A", 140_000L), entry("B", -20_000L), entry("C", -40_000L), entry("D", -80_000L));
    }

    @Test
    @DisplayName("잔액 합계는 항상 0")
    void balancesSumToZero() {
        Map<String, Long> balances = calculator.calculateBalances(ABCD, SAMPLE_EXPENSES);

        assertThat(balances.values().stream().mapToLong(Long::longValue).sum()).isZero();
    }

    @Test
    @DisplayName("기획서 예시: 송금 3건으로 정산")
    void sampleTransfers() {
        Map<String, Long> balances = calculator.calculateBalances(ABCD, SAMPLE_EXPENSES);

        assertThat(calculator.settle(balances)).containsExactly(
                new Transfer("D", "A", 80_000),
                new Transfer("C", "A", 40_000),
                new Transfer("B", "A", 20_000));
    }

    @Test
    @DisplayName("나누어떨어지지 않는 금액: 나머지는 결제자가 부담")
    void remainderPaidByPayer() {
        List<Expense> expenses = List.of(new Expense("A", 100_000, List.of("A", "B", "C")));

        Map<String, Long> balances = calculator.calculateBalances(List.of("A", "B", "C"), expenses);

        // A 몫 33,334 / B, C 몫 33,333
        assertThat(balances).containsOnly(
                entry("A", 66_666L), entry("B", -33_333L), entry("C", -33_333L));
    }

    @Test
    @DisplayName("나누어떨어지지 않는 금액: 결제자가 참여자가 아니면 참여자 중 첫 번째가 부담")
    void remainderPaidByFirstParticipantWhenPayerNotParticipating() {
        List<Expense> expenses = List.of(new Expense("D", 100_000, List.of("A", "B", "C")));

        Map<String, Long> balances = calculator.calculateBalances(ABCD, expenses);

        assertThat(balances).containsOnly(
                entry("A", -33_334L), entry("B", -33_333L), entry("C", -33_333L), entry("D", 100_000L));
    }

    @Test
    @DisplayName("본인 몫만 결제한 멤버는 잔액 0이고 송금 목록에서 제외")
    void zeroBalanceMemberExcludedFromTransfers() {
        List<Expense> expenses = List.of(
                new Expense("A", 30_000, List.of("A", "B", "C")),
                new Expense("D", 10_000, List.of("D")));

        Map<String, Long> balances = calculator.calculateBalances(ABCD, expenses);

        assertThat(balances).containsEntry("D", 0L);
        assertThat(calculator.settle(balances)).containsExactly(
                new Transfer("B", "A", 10_000),
                new Transfer("C", "A", 10_000));
    }

    @Test
    @DisplayName("한 사람이 모든 지출을 결제하면 나머지 인원이 각자 한 번씩 송금 (인원 - 1건)")
    void onePayerForAllExpenses() {
        List<Expense> expenses = List.of(
                new Expense("A", 40_000, ABCD),
                new Expense("A", 20_000, ABCD));

        Map<String, Long> balances = calculator.calculateBalances(ABCD, expenses);

        assertThat(calculator.settle(balances)).containsExactly(
                new Transfer("B", "A", 15_000),
                new Transfer("C", "A", 15_000),
                new Transfer("D", "A", 15_000));
    }

    @Test
    @DisplayName("지출이 0건이면 모두 잔액 0, 송금 없음")
    void noExpenses() {
        Map<String, Long> balances = calculator.calculateBalances(ABCD, List.of());

        assertThat(balances).containsOnly(
                entry("A", 0L), entry("B", 0L), entry("C", 0L), entry("D", 0L));
        assertThat(calculator.settle(balances)).isEmpty();
    }

    @Test
    @DisplayName("가장 큰 채권자와 가장 큰 채무자를 반복 매칭")
    void greedyMatchesLargestCreditorAndDebtor() {
        Map<String, Long> balances = new LinkedHashMap<>();
        balances.put("A", 50L);
        balances.put("B", 30L);
        balances.put("C", -60L);
        balances.put("D", -20L);

        assertThat(calculator.settle(balances)).containsExactly(
                new Transfer("C", "A", 50),
                new Transfer("D", "B", 20),
                new Transfer("C", "B", 10));
    }

    @Test
    @DisplayName("일부 송금 완료 후 남은 송금 재계산")
    void recalculateAfterCompletedTransfer() {
        Map<String, Long> balances = calculator.calculateBalances(ABCD, SAMPLE_EXPENSES);

        Map<String, Long> remaining = calculator.applyTransfers(
                balances, List.of(new Transfer("D", "A", 80_000)));

        assertThat(remaining).containsOnly(
                entry("A", 60_000L), entry("B", -20_000L), entry("C", -40_000L), entry("D", 0L));
        assertThat(calculator.settle(remaining)).containsExactly(
                new Transfer("C", "A", 40_000),
                new Transfer("B", "A", 20_000));
    }
}
