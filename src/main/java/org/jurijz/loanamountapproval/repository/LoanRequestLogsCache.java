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

    public void add(final LoanRequestLog loanRequestLog) {
        final LocalDateTime logHour = loanRequestLog.getSentToCustomerTime().truncatedTo(ChronoUnit.HOURS);
        cache.computeIfAbsent(logHour, key -> new ArrayList<>()).add(loanRequestLog);
    }

    public List<LoanRequestLog> getByPeriod(final LocalDateTime periodStart, final LocalDateTime periodEnd) {
        final LocalDateTime hourStart = periodStart.truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime hourEnd = periodEnd.truncatedTo(ChronoUnit.HOURS);
        if (hourStart.equals(hourEnd)) {
            return cache.getOrDefault(hourStart, new ArrayList<>()).stream()
                    .filter(log -> log.getSentToCustomerTime().isAfter(periodStart) &&
                            log.getSentToCustomerTime().isBefore(periodEnd))
                    .collect(Collectors.toList());
        }
        final List<LocalDateTime> hours = getHoursBetween(periodStart, periodEnd);

        return Stream.of(
                cache.getOrDefault(hourStart, new ArrayList<>()).stream().filter(log -> log.getSentToCustomerTime().isAfter(periodStart)),
                hours.stream().map(hour -> cache.getOrDefault(hour, new ArrayList<>())).flatMap(List::stream),
                cache.getOrDefault(hourEnd, new ArrayList<>()).stream().filter(log -> log.getSentToCustomerTime().isBefore(periodEnd))
        ).reduce(Stream::concat).orElseGet(Stream::empty).collect(Collectors.toList());
    }

    private List<LocalDateTime> getHoursBetween(final LocalDateTime hourStart, final LocalDateTime hourEnd) {
        final List<LocalDateTime> hours = new ArrayList<>();
        boolean hasNext = true;
        int i = 0;
        while (hasNext) {
            final LocalDateTime nextHour = hourStart.plusHours(i++);
            hours.add(nextHour);
            hasNext = !nextHour.equals(hourEnd);
        }
        return hours;
    }
}
