package org.jurijz.loanamountapproval.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jurijz.loanamountapproval.converter.LoanRequestConverter;
import org.jurijz.loanamountapproval.converter.ManagerApprovalConverter;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanRequestServiceTest {

    private LoanRequestService service;
    private LoanRequestsCache cache;

    @Mock
    private LoanRequestLogsCache loanRequestLogsCache;
    @Mock
    private LoanExternalService loanExternalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cache = new LoanRequestsCache();
        final LoanRequestConverter loanRequestConverter = new LoanRequestConverter(new ManagerApprovalConverter());
        service = new LoanRequestService(cache, loanRequestLogsCache, loanRequestConverter, loanExternalService);
    }

    @DisplayName("Given request dtos when process request then save to cache and send to managers")
    @Test
    void processRequestSuccess() {
        final String givenCustomerId = "XX-XXXX-XX0";
        final Set<String> givenApprovers = Set.of("aaa", "bbb", "ccc");
        final List<LoanRequestDto> givenRequests = IntStream.range(0, 4)
                .mapToObj(index -> LoanRequestDto.builder()
                        .customerId(givenCustomerId.replace("0",String.valueOf(index)))
                        .amount(new BigDecimal(index))
                        .approvers(givenApprovers).build()).collect(Collectors.toList());

        final LoanRequest expected = new LoanRequest();
        expected.setAmount(new BigDecimal(0));
        expected.setCustomerId(givenCustomerId);
        expected.setManagerApprovals(Set.of(new ManagerApproval("aaa", false),
                new ManagerApproval("bbb", false), new ManagerApproval("ccc", false)));

        service.processRequests(givenRequests);
        final LoanRequest actual = cache.get(givenCustomerId);

        assertThat(actual).isEqualTo(expected);

        verify(loanExternalService, times(12)).sendToManager(any(NotificationToManagerDto.class));
        verifyNoMoreInteractions(loanRequestLogsCache, loanExternalService);

    }

    @DisplayName("Given request which is already pending when process request then throw exception")
    @Test
    void processRequestWithPendingRequestFail() {
        final String givenCustomerId = "XX-XXXX-XXX";
        final Set<String> givenApprovers = Set.of("aaa");
        final LoanRequestDto givenDto = LoanRequestDto.builder()
                .customerId(givenCustomerId)
                .amount(new BigDecimal(10))
                .approvers(givenApprovers).build();
        final List<LoanRequestDto> givenRequests = List.of(givenDto, givenDto);

        final LoanRequest expected = new LoanRequest();
        expected.setAmount(new BigDecimal(10));
        expected.setCustomerId(givenCustomerId);
        expected.setManagerApprovals(Set.of(new ManagerApproval("aaa", false)));

        assertThatThrownBy(() -> service.processRequests(givenRequests))
                .isInstanceOf(CustomerLoanApprovalException.class)
                .hasMessageContaining("There are still pending requests for customers:");

        final LoanRequest actual = cache.get(givenCustomerId);

        assertThat(actual).isEqualTo(expected);

        verify(loanExternalService, times(1)).sendToManager(any(NotificationToManagerDto.class));
        verifyNoMoreInteractions(loanRequestLogsCache, loanExternalService);
    }

    @DisplayName("Given request in cache when not all managers approve then not send to customer")
    @Test
    void approveRequestWithNotAllApprovalsSuccess() {
        final String givenCustomerId = "XX-XXXX-XXX";
        final LoanRequest given = new LoanRequest();
        given.setManagerApprovals(Set.of(new ManagerApproval("aaa", false),
                new ManagerApproval("bbb", false)));
        given.setAmount(new BigDecimal(1));
        given.setCustomerId(givenCustomerId);

        cache.add(given);

        final ManagerApprovalDto givenApprovalDto = new ManagerApprovalDto();
        givenApprovalDto.setUsername("aaa");
        givenApprovalDto.setCustomerId(givenCustomerId);

        service.approveRequest(givenApprovalDto);

        verifyNoMoreInteractions(loanExternalService, loanRequestLogsCache);
    }

    @DisplayName("Given request in cache when all managers approve then send to customer")
    @Test
    void approveRequestWithAllApprovalsSuccess() {
        final String givenCustomerId = "XX-XXXX-XXX";
        final LoanRequest given = new LoanRequest();
        given.setManagerApprovals(Set.of(new ManagerApproval("aaa", false),
                new ManagerApproval("bbb", true)));
        given.setAmount(new BigDecimal(1));
        given.setCustomerId(givenCustomerId);

        cache.add(given);

        final ManagerApprovalDto givenApprovalDto = new ManagerApprovalDto();
        givenApprovalDto.setUsername("aaa");
        givenApprovalDto.setCustomerId(givenCustomerId);

        final NotificationToCustomerDto expected = new NotificationToCustomerDto();
        expected.setAmount(new BigDecimal(1));
        expected.setCustomerId(givenCustomerId);

        service.approveRequest(givenApprovalDto);

        verify(loanExternalService, times(1)).sendToCustomer(expected);
        verify(loanRequestLogsCache, times(1)).add(any(LoanRequestLog.class));
        verifyNoMoreInteractions(loanExternalService, loanRequestLogsCache);
    }

    @DisplayName("Given request in cache when bad manager username approve then throw exception")
    @Test
    void approveRequestBadManagerUsernameFail() {
        final String givenCustomerId = "XX-XXXX-XXX";
        final LoanRequest given = new LoanRequest();
        given.setManagerApprovals(Set.of(new ManagerApproval("aaa", false),
                new ManagerApproval("bbb", false)));
        given.setAmount(new BigDecimal(1));
        given.setCustomerId(givenCustomerId);

        cache.add(given);

        final ManagerApprovalDto givenApprovalDto = new ManagerApprovalDto();
        givenApprovalDto.setUsername("ddd");
        givenApprovalDto.setCustomerId(givenCustomerId);

        assertThatThrownBy(() -> service.approveRequest(givenApprovalDto))
        .isInstanceOf(ManagerApprovalException.class)
        .hasMessageContaining(" is not among customer ")
        .hasMessageContaining(" approvers.");

        verifyNoMoreInteractions(loanExternalService, loanRequestLogsCache);
    }
}