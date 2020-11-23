package org.jurijz.loanamountapproval.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jurijz.loanamountapproval.domain.LoanRequestLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRequestLogsCacheTest {

    private LoanRequestLogsCache cache;

    @BeforeEach
    void setUp() {
        cache = new LoanRequestLogsCache();
    }

    @DisplayName("Given log when add to cache then retrieve")
    @Test
    void addSuccess() {
        final LocalDateTime givenTime = LocalDateTime.now();
        final LocalDateTime givenPeriodStart = givenTime.minusSeconds(60);
        final LocalDateTime givenPeriodEnd = givenTime.plusSeconds(60);
        final LoanRequestLog given = LoanRequestLog.builder().amount(new BigDecimal(1))
                .sentToCustomerTime(givenTime).build();
        final LoanRequestLog expected = LoanRequestLog.builder().amount(new BigDecimal(1))
                .sentToCustomerTime(givenTime).build();

        cache.add(given);

        final LoanRequestLog actual = cache.getByPeriod(givenPeriodStart, givenPeriodEnd)
                .stream().findFirst().orElseThrow();
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("Given many logs when added then get by period of same hour")
    @Test
    void getByPeriodWithSameHourSuccess() {
        final int expectedSize = 20;
        final LocalDateTime givenTime = LocalDateTime.now().withMinute(0).withSecond(1);
        final LocalDateTime givenPeriodStart = givenTime.withSecond(0);
        final LocalDateTime givenPeriodEnd = givenTime.plusSeconds(60);

        IntStream.range(0, expectedSize)
                .mapToObj(index -> LoanRequestLog.builder().amount(new BigDecimal(index))
                    .sentToCustomerTime(givenTime.plusSeconds(index)).build())
                .forEach(log -> cache.add(log));

        final List<LoanRequestLog> actual = cache.getByPeriod(givenPeriodStart, givenPeriodEnd);

        assertThat(actual).asList().hasSize(expectedSize);
    }

    @DisplayName("Given many logs when added then get by period of adjacent hours")
    @Test
    void getByPeriodWithAdjacentHoursSuccess() {
        final int expectedSize = 20;
        final LocalDateTime givenTime = LocalDateTime.now().minusHours(1).withMinute(0).withSecond(1);
        final LocalDateTime givenPeriodStart = givenTime.withSecond(0);
        final LocalDateTime givenPeriodEnd = givenTime.plusHours(1).plusSeconds(60);

        IntStream.range(0, expectedSize)
                .mapToObj(index -> LoanRequestLog.builder().amount(new BigDecimal(index))
                        .sentToCustomerTime(givenTime.plusHours(index / 10).plusSeconds(index)).build())
                .forEach(log -> cache.add(log));

        final List<LoanRequestLog> actual = cache.getByPeriod(givenPeriodStart, givenPeriodEnd);

        assertThat(actual).asList().hasSize(20);
    }

    @DisplayName("Given many logs when added then get by period of 10 hours")
    @Test
    void getByPeriodWithManyHoursSuccess() {
        final int expectedSize = 20;
        final LocalDateTime givenTime = LocalDateTime.now().minusHours(4).withMinute(0).withSecond(1);
        final LocalDateTime givenPeriodStart = givenTime.minusHours(1).withSecond(0);
        final LocalDateTime givenPeriodEnd = givenTime.plusHours(9).plusSeconds(60);

        IntStream.range(0, expectedSize)
                .mapToObj(index -> LoanRequestLog.builder().amount(new BigDecimal(index))
                        .sentToCustomerTime(givenTime.plusHours(index / 2).plusSeconds(index)).build())
                .forEach(log -> cache.add(log));

        final List<LoanRequestLog> actual = cache.getByPeriod(givenPeriodStart, givenPeriodEnd);

        assertThat(actual).asList().hasSize(20);
    }

    @DisplayName("Given no logs when get then empty result")
    @Test
    void getByPeriodWithNoLogsEmptyResult() {

        final List<LoanRequestLog> actual = cache.getByPeriod(LocalDateTime.now().minusSeconds(60), LocalDateTime.now());

        assertThat(actual).asList().isEmpty();
    }
}