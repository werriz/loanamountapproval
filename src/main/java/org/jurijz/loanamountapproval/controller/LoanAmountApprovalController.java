package org.jurijz.loanamountapproval.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jurijz.loanamountapproval.domain.LoanApprovalStatistics;
import org.jurijz.loanamountapproval.domain.dto.LoanAmountApprovalRequest;
import org.jurijz.loanamountapproval.domain.dto.ManagerApprovalDto;
import org.jurijz.loanamountapproval.exception.CustomerLoanApprovalException;
import org.jurijz.loanamountapproval.exception.ManagerApprovalException;
import org.jurijz.loanamountapproval.exception.StatisticsPeriodException;
import org.jurijz.loanamountapproval.service.LoanRequestService;
import org.jurijz.loanamountapproval.service.LoanStatisticsService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("loan")
@RequiredArgsConstructor
public class LoanAmountApprovalController {

    private final LoanRequestService loanRequestService;
    private final LoanStatisticsService loanStatisticsService;

    @PostMapping(path = "/requests", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createLoanAmountApprovalRequest(@Valid @NotNull @RequestBody final LoanAmountApprovalRequest request) {
        loanRequestService.processRequests(request.getRequests());
    }

    @PutMapping(path = "/approvals", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void approveLoanAmountRequest(@Valid @RequestBody final ManagerApprovalDto approvalDto) {
        loanRequestService.approveRequest(approvalDto);
    }

    @ApiResponse(responseCode = "400", description = "Bad request")
    @GetMapping(path = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoanApprovalStatistics getStatistics(@RequestParam(required = false, name = "periodStart") final String periodStart,
                                                @RequestParam(required = false, name = "periodEnd") final String periodEnd) {
        log.info("Request for statistics received, start: {}, end: {}", periodStart, periodEnd);
        return loanStatisticsService.gatherStatistics(periodStart, periodEnd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleException(final MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(exception.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("\n")));
    }

    @ExceptionHandler({CustomerLoanApprovalException.class, ManagerApprovalException.class, StatisticsPeriodException.class})
    public ResponseEntity<String> handleException(final RuntimeException exception) {
        return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(exception.getMessage());
    }
}
