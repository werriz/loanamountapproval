package org.jurijz.loanamountapproval.repository;

import org.jurijz.loanamountapproval.domain.LoanRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoanRequestsCache {

    private final Map<String, LoanRequest> cache = new HashMap<>();

    /**
     * Stores loan request object in cache.
     * @param loanRequest LoanRequest object
     * @return LoanRequest stored in cache object
     */
    public LoanRequest add(final LoanRequest loanRequest) {
        cache.put(loanRequest.getCustomerId(), loanRequest);
        return loanRequest;
    }

    /**
     * Get loan request from cache by customer id.
     * @param customerId String customer id
     * @return LoanRequest object
     */
    public LoanRequest get(final String customerId) {
        return cache.get(customerId);
    }

    /**
     * Removes LoanRequest from cache by customer id.
     * @param customerId String customer id
     */
    public void remove(final String customerId) {
        cache.remove(customerId);
    }
}
