package org.jurijz.loanamountapproval.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CustomerLoanApprovalException extends RuntimeException {

    private final String message;
}
