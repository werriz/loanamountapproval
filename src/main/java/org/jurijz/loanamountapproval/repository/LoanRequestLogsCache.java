package org.jurijz.loanamountapproval.repository;

import org.jurijz.loanamountapproval.domain.LoanRequestLog;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LoanRequestLogsCache {

    private final Map<LocalDateTime, List<LoanRequestLog>> cache = new HashMap<>();

    /**
     * Stores log object in cache.
     * @param loanRequestLog LoanRequestLog object
     */
    public void add(final LoanRequestLog loanRequestLog) {
        final LocalDateTime logHour = loanRequestLog.getSentToCustomerTime().truncatedTo(ChronoUnit.HOURS);
        cache.computeIfAbsent(logHour, key -> new ArrayList<>()).add(loanRequestLog);
    }

    /**
     * Get logs by time period, both periods should be non-null and start before end.
     * @param periodStart LocalDateTime start
     * @param periodEnd LocalDateTime end
     * @return List<LoanRequestLog> list of logs in period
     */
    public List<LoanRequestLog> getByPeriod(final LocalDateTime periodStart, final LocalDateTime periodEnd) {
        final LocalDateTime hourStart = periodStart.truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime hourEnd = periodEnd.truncatedTo(ChronoUnit.HOURS);
        if (hourStart.equals(hourEnd)) {
            return cache.getOrDefault(hourStart, new ArrayList<>()).stream()
                    .filter(log -> log.getSentToCustomerTime().isAfter(periodStart) &&
                            log.getSentToCustomerTime().isBefore(periodEnd))
                    .collect(Collectors.toList());
        }
        final List<LocalDateTime> hours = getHoursBetween(hourStart, hourEnd);

        return Stream.of(
                cache.getOrDefault(hourStart, new ArrayList<>()).stream().filter(log -> log.getSentToCustomerTime().isAfter(periodStart)),
                hours.stream().map(hour -> cache.getOrDefault(hour, new ArrayList<>())).flatMap(List::stream),
                cache.getOrDefault(hourEnd, new ArrayList<>()).stream().filter(log -> log.getSentToCustomerTime().isBefore(periodEnd))
        ).reduce(Stream::concat).orElseGet(Stream::empty).collect(Collectors.toList());
    }

    private List<LocalDateTime> getHoursBetween(final LocalDateTime hourStart, final LocalDateTime hourEnd) {
        final List<LocalDateTime> hours = new ArrayList<>();
        int i = 1;
        while (true) {
            final LocalDateTime nextHour = hourStart.plusHours(i++);
            if (nextHour.equals(hourEnd)) {
                break;
            }
            hours.add(nextHour);
        }
        return hours;
    }
}
