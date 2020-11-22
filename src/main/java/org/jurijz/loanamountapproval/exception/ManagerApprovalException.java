package org.jurijz.loanamountapproval.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ManagerApprovalException extends RuntimeException {

    private final String message;
}
