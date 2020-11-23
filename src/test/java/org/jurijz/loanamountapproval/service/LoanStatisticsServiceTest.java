package org.jurijz.loanamountapproval.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jurijz.loanamountapproval.domain.LoanApprovalStatistics;
import org.jurijz.loanamountapproval.domain.LoanRequestLog;
import org.jurijz.loanamountapproval.exception.StatisticsPeriodException;
import org.jurijz.loanamountapproval.repository.LoanRequestLogsCache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanStatisticsServiceTest {

    private LoanStatisticsService service;
    private LoanRequestLogsCache cache;

    @BeforeEach
    void setUp() {
        cache = new LoanRequestLogsCache();
        service = new LoanStatisticsService(cache);
    }

    @DisplayName("Given logs when gather statistics then get statistics object")
    @Test
    void gatherStatisticsWithPeriodSuccess() {
        final String givenPeriodStart = "2020-01-01 00:00:00";
        final String givenPeriodEnd = "2020-01-01 00:01:00";
        final LocalDateTime givenTime = LocalDateTime.of(2020, 1, 1, 0,0,1);
        final int expectedSize = 20;

        final LoanApprovalStatistics expected = new LoanApprovalStatistics();
        expected.setCount(20);
        expected.setSum(new BigDecimal(190));
        expected.setAvg(BigDecimal.valueOf(9.5).setScale(2, RoundingMode.HALF_UP));
        expected.setMax(new BigDecimal(19));
        expected.setMin(new BigDecimal(0));

        IntStream.range(0, expectedSize).mapToObj(index -> LoanRequestLog.builder().amount(new BigDecimal(index))
                .sentToCustomerTime(givenTime.plusSeconds(index).plusSeconds(index)).build())
                .forEach(log -> cache.add(log));

        final LoanApprovalStatistics actual = service.gatherStatistics(givenPeriodStart, givenPeriodEnd);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("Given no logs when gather statistics then get empty statistics object")
    @Test
    void gatherStatisticsWithNoLogsEmpty() {
        final String givenPeriodStart = "2020-01-01 00:00:00";
        final String givenPeriodEnd = "2020-01-01 00:01:00";

        final LoanApprovalStatistics expected = new LoanApprovalStatistics();
        expected.setCount(0);

        final LoanApprovalStatistics actual = service.gatherStatistics(givenPeriodStart, givenPeriodEnd);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("Given logs and default dates when gather statistics then get object")
    @Test
    void gatherStatisticsWithDefaultPeriodSuccess() {
        final String givenPeriodStart = "";
        final String givenPeriodEnd = null;
        final LocalDateTime givenTime = LocalDateTime.now().minusSeconds(55);

        final int expectedSize = 20;

        final LoanApprovalStatistics expected = new LoanApprovalStatistics();
        expected.setCount(20);
        expected.setSum(new BigDecimal(380));
        expected.setAvg(BigDecimal.valueOf(19).setScale(2, RoundingMode.HALF_UP));
        expected.setMax(new BigDecimal(38));
        expected.setMin(new BigDecimal(0));

        IntStream.range(0, expectedSize).mapToObj(index -> LoanRequestLog.builder().amount(new BigDecimal(index * 2))
                .sentToCustomerTime(givenTime.plusSeconds(index).plusSeconds(index)).build())
                .forEach(log -> cache.add(log));

        final LoanApprovalStatistics actual = service.gatherStatistics(givenPeriodStart, givenPeriodEnd);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("Given start after end when gather stats then throw exception")
    @Test
    void gatherStatisticsWithBadPeriodFail() {
        final String givenStartDate = "2020-01-01 00:00:00";
        final String givenEndDate = "2019-01-01 00:00:00";
        assertThatThrownBy(() -> service.gatherStatistics(givenStartDate, givenEndDate))
                .isInstanceOf(StatisticsPeriodException.class)
                .hasMessageContaining("Period start cannot be after period end.");
    }

    @DisplayName("Given bad format start and end dates when gather stats then throw exception")
    @Test
    void gatherStatisticsWithBadFormatPeriodFail() {
        final String givenStartDate = "badFormatStartDate";
        final String givenEndDate = "badFormatEndDate";
        assertThatThrownBy(() -> service.gatherStatistics(givenStartDate, givenEndDate))
                .isInstanceOf(StatisticsPeriodException.class)
                .hasMessageContaining("should match time format pattern");
    }
}