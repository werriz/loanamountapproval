package org.jurijz.loanamountapproval.domain.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
public class LoanAmountApprovalRequest {

    @Valid
    private List<LoanRequestDto> requests;

}
