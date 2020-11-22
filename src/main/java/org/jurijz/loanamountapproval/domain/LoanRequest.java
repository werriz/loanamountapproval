package org.jurijz.loanamountapproval.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class LoanRequest {

    private String customerId;
    private BigDecimal amount;
    private Set<ManagerApproval> managerApprovals;

}
