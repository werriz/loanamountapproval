package org.jurijz.loanamountapproval.repository;

import lombok.SneakyThrows;
import org.jurijz.loanamountapproval.domain.LoanRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoanRequestsCache {

    private final Map<String, LoanRequest> cache = new HashMap<>();

    @SneakyThrows
    public LoanRequest add(final LoanRequest loanRequest) {
        cache.put(loanRequest.getCustomerId(), loanRequest);
        return loanRequest;
    }

    public LoanRequest get(final String customerId) {
        return cache.get(customerId);
    }

    public void remove(final String customerId) {
        cache.remove(customerId);
    }
}
