package org.jurijz.loanamountapproval.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class ManagerApprovalDto {

    @NotBlank(message = "Managers username cannot be empty.")
    private String username;
    @NotBlank(message = "Customer Id cannot be empty.")
    @Pattern(regexp = "(?i)[A-Z0-9]{2}[-][A-Z0-9]{4}[-][A-Z0-9]{3}$",
            message = "Customer id must match pattern 'XX-XXXX-XXX'.")
    private String customerId;

}
