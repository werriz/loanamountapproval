package org.jurijz.loanamountapproval.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequestDto {

    @NotBlank(message = "Customer Id cannot be empty.")
    @Pattern(regexp = "(?i)[A-Z0-9]{2}[-][A-Z0-9]{4}[-][A-Z0-9]{3}$",
            message = "Customer id must match pattern 'XX-XXXX-XXX'.")
    private String customerId;
    @NotNull(message = "Amount cannot be null.")
    private BigDecimal amount;
    @Size(min = 1, message = "Approvers cannot be less than 1.")
    @Size(max = 3, message = "Approvers cannot be more than 3.")
    private Set<String> approvers;
}
