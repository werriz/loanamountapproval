package org.jurijz.loanamountapproval.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationToCustomerDto {

    private String customerId;
    private BigDecimal amount;

}
