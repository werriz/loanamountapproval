package org.jurijz.loanamountapproval.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoanRequestLog {

    private BigDecimal amount;
    private LocalDateTime sentToCustomerTime;
}
