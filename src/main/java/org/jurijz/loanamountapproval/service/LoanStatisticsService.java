package org.jurijz.loanamountapproval.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jurijz.loanamountapproval.domain.LoanApprovalStatistics;
import org.jurijz.loanamountapproval.domain.LoanRequestLog;
import org.jurijz.loanamountapproval.exception.StatisticsPeriodException;
import org.jurijz.loanamountapproval.repository.LoanRequestLogsCache;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanStatisticsService {

    private static final String DATE_FORMAT = "yyyy-MMM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private final LoanRequestLogsCache loanRequestLogsCache;

    public LoanApprovalStatistics gatherStatistics(final String periodStartStr, final String periodEndStr) {

        final LocalDateTime periodStart = convertToTime(periodStartStr, LocalDateTime.now().minusSeconds(60));
        final LocalDateTime periodEnd = convertToTime(periodEndStr, LocalDateTime.now());

        if (periodStart.isAfter(periodEnd)) {
            throw new StatisticsPeriodException(String.format("Period start cannot be after period end. %s > %s",
                    DATE_TIME_FORMATTER.format(periodStart), DATE_TIME_FORMATTER.format(periodEnd)));
        }
        final List<LoanRequestLog> logs = loanRequestLogsCache.getByPeriod(periodStart, periodEnd);

        final LoanApprovalStatistics loanStatistics = new LoanApprovalStatistics();
        loanStatistics.setCount(logs.size());
        if (!logs.isEmpty()) {
            BigDecimal max = new BigDecimal(0);
            BigDecimal min = new BigDecimal(Integer.MAX_VALUE);
            BigDecimal sum = new BigDecimal(0);
            for (final LoanRequestLog log : logs) {
                final BigDecimal amount = log.getAmount();
                sum = sum.add(amount);
                if (amount.compareTo(max) > 0) {
                    max = amount;
                } else if (amount.compareTo(min) < 0) {
                    min = amount;
                }
            }
            loanStatistics.setMax(max);
            loanStatistics.setMax(min);
            loanStatistics.setSum(sum);
            loanStatistics.setAvg(sum.divide(new BigDecimal(logs.size()), 2, RoundingMode.HALF_UP));
        }

        return loanStatistics;
    }

    private LocalDateTime convertToTime(final String dateTimeStr, final LocalDateTime defaultTime) {
        if (dateTimeStr == null) {
            return defaultTime;
        }

        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (final DateTimeParseException exception) {
            throw new StatisticsPeriodException(String.format("%s should match time format pattern '%s'",
                    dateTimeStr, DATE_FORMAT));
        }

    }
}
