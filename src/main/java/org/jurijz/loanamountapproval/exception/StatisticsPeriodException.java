package org.jurijz.loanamountapproval.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class StatisticsPeriodException extends RuntimeException {

    private final String message;
}
