package org.jurijz.loanamountapproval.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jurijz.loanamountapproval.converter.LoanRequestConverter;
import org.jurijz.loanamountapproval.domain.LoanRequest;
import org.jurijz.loanamountapproval.domain.LoanRequestLog;
import org.jurijz.loanamountapproval.domain.ManagerApproval;
import org.jurijz.loanamountapproval.domain.dto.LoanRequestDto;
import org.jurijz.loanamountapproval.domain.dto.ManagerApprovalDto;
import org.jurijz.loanamountapproval.domain.dto.NotificationToCustomerDto;
import org.jurijz.loanamountapproval.domain.dto.NotificationToManagerDto;
import org.jurijz.loanamountapproval.exception.CustomerLoanApprovalException;
import org.jurijz.loanamountapproval.exception.ManagerApprovalException;
import org.jurijz.loanamountapproval.repository.LoanRequestLogsCache;
import org.jurijz.loanamountapproval.repository.LoanRequestsCache;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanRequestService {

    private final LoanRequestsCache loanRequestsCache;
    private final LoanRequestLogsCache loanRequestLogsCache;
    private final LoanRequestConverter loanRequestConverter;
    private final LoanExternalService loanExternalService;

    /**
     * Process requests received from loan preparators, stores in cache and then send manager a notification.
     * @param requests List<LoanRequestDto> list of requests
     * @throws CustomerLoanApprovalException exception thrown after all request processed and if any request has already
     * pending in cache
     */
    public void processRequests(final List<LoanRequestDto> requests) {
        final List<String> failedRequests = new ArrayList<>();
        requests.forEach(dto -> {
            log.info("Received request: {}", dto);
            final String customerId = dto.getCustomerId();
            if (loanRequestsCache.get(dto.getCustomerId()) != null) {
                log.warn("Cannot process customer {} request, because older request is still pending.", customerId);
                failedRequests.add(customerId);
            } else {
                final LoanRequest loanRequest = loanRequestsCache.add(loanRequestConverter.convert(dto));

                loanRequest.getManagerApprovals().forEach(managerApproval -> loanExternalService.sendToManager(
                        createNotificationDto(managerApproval.getUsername(), customerId, loanRequest.getAmount())));
            }
        });
        if (!failedRequests.isEmpty()) {
            throw new CustomerLoanApprovalException(String.format("There are still pending requests for customers: %s",
                    String.join(",", failedRequests)));
        }
    }

    /**
     * Process loan request approval received from manager, if all approvals true then sends notification to customer.
     * @param approvalDto ManagerApprovalDto object containing manager username and customer id
     * @throws ManagerApprovalException thrown when manager username is not loan requests approvers list
     */
    public void approveRequest(final ManagerApprovalDto approvalDto) {
        final LoanRequest request = loanRequestsCache.get(approvalDto.getCustomerId());
        if (!isRequestApprover(approvalDto.getUsername(), request.getManagerApprovals())) {
            throw new ManagerApprovalException(String.format("Manager %s is not among customer %s approvers.",
                    approvalDto.getUsername(), approvalDto.getUsername()));
        }
        approveByManager(request, approvalDto.getUsername());
        if (hasAllApprovals(request.getManagerApprovals())) {
            loanExternalService.sendToCustomer(createNotificationToCustomer(request.getCustomerId(), request.getAmount()));
            loanRequestLogsCache.add(LoanRequestLog.builder()
                    .amount(request.getAmount())
                    .sentToCustomerTime(LocalDateTime.now())
                    .build());
            loanRequestsCache.remove(request.getCustomerId());
        }
    }

    private boolean isRequestApprover(final String username, final Set<ManagerApproval> managerApprovals) {
        return managerApprovals.stream().anyMatch(managerApproval -> managerApproval.getUsername().equals(username));
    }

    private NotificationToManagerDto createNotificationDto(final String username, final String customerId, final BigDecimal amount) {
        return NotificationToManagerDto.builder()
                .username(username)
                .customerId(customerId)
                .amount(amount)
                .build();
    }

    private void approveByManager(final LoanRequest request, final String username) {
        request.getManagerApprovals()
                .stream()
                .filter(approval -> approval.getUsername().equals(username))
                .limit(1)
                .findFirst()
                .ifPresent(approval -> approval.setApproved(true));
    }

    private boolean hasAllApprovals(final Set<ManagerApproval> approvals) {
        return approvals.stream().allMatch(ManagerApproval::isApproved);
    }

    private NotificationToCustomerDto createNotificationToCustomer(final String customerId, final BigDecimal amount) {
        return NotificationToCustomerDto.builder()
                .customerId(customerId)
                .amount(amount)
                .build();
    }
}
