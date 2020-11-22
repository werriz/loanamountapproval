package org.jurijz.loanamountapproval.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApprovalStatistics {

    private int count;
    private BigDecimal sum;
    private BigDecimal avg;
    private BigDecimal max;
    private BigDecimal min;

}
