package org.jurijz.loanamountapproval.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jurijz.loanamountapproval.converter.LoanRequestConverter;
import org.jurijz.loanamountapproval.converter.ManagerApprovalConverter;
import org.jurijz.loanamountapproval.domain.LoanRequest;
import org.jurijz.loanamountapproval.domain.LoanRequestLog;
import org.jurijz.loanamountapproval.domain.ManagerApproval;
import org.jurijz.loanamountapproval.domain.dto.*;
import org.jurijz.loanamountapproval.repository.LoanRequestLogsCache;
import org.jurijz.loanamountapproval.repository.LoanRequestsCache;
import org.jurijz.loanamountapproval.service.LoanExternalService;
import org.jurijz.loanamountapproval.service.LoanRequestService;
import org.jurijz.loanamountapproval.service.LoanStatisticsService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoanAmountApprovalControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String URL = "/loan";

    @Mock
    private LoanExternalService loanExternalService;

    private MockMvc mockMvc;
    private LoanRequestsCache requestsCache;
    private LoanRequestLogsCache logsCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestsCache = new LoanRequestsCache();
        logsCache = new LoanRequestLogsCache();
        final LoanRequestConverter loanRequestConverter = new LoanRequestConverter(new ManagerApprovalConverter());
        final LoanAmountApprovalController controller = new LoanAmountApprovalController(
                new LoanRequestService(requestsCache, logsCache, loanRequestConverter, loanExternalService),
                new LoanStatisticsService(logsCache));

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @DisplayName("Given valid request when create request then get from cache send to manager")
    @Test
    void createLoanAmountRequestSuccess() throws Exception {
        final String givenCustomerId = "XX-XXXX-XXX";
        final BigDecimal givenAmount = new BigDecimal(155);
        final LoanAmountApprovalRequest given = new LoanAmountApprovalRequest();
        given.setRequests(List.of(LoanRequestDto.builder().customerId("XX-XXXX-XXX")
                .amount(givenAmount).approvers(Set.of("aaa", "bbb")).build()));

        final LoanRequest expected = new LoanRequest();
        expected.setCustomerId(givenCustomerId);
        expected.setAmount(givenAmount);
        expected.setManagerApprovals(Set.of(new ManagerApproval("aaa", false),
                new ManagerApproval("bbb", false)));

        final NotificationToManagerDto expectedNotification = NotificationToManagerDto.builder()
                .customerId(givenCustomerId).amount(givenAmount).username("aaa").build();

        mockMvc.perform(MockMvcRequestBuilders.post(URL + "/requests").content(toJson(given))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(requestsCache.get(givenCustomerId)).isEqualTo(expected);

        verify(loanExternalService, times(2)).sendToManager(argThat(notificationDto -> {
            notificationDto.setUsername("aaa");
            return notificationDto.equals(expectedNotification);
        }));
        verifyNoMoreInteractions(loanExternalService);
    }

    @DisplayName("Given invalid request when create request then get bad request")
    @Test
    void createLoanAmountRequestFail() throws Exception  {

        final LoanAmountApprovalRequest given = new LoanAmountApprovalRequest();
        given.setRequests(List.of(LoanRequestDto.builder().customerId("!X-XXXX1XXX")
                .approvers(Set.of("aaa", "bbb", "ccc", "ddd")).build()));
        final String actual = mockMvc.perform(MockMvcRequestBuilders.post(URL+"/requests").content(toJson(given))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        assertThat(actual).contains("Customer id must match pattern 'XX-XXXX-XXX'.")
                .contains("Amount cannot be null.")
                .contains("Approvers cannot be more than 3.");
    }

    @DisplayName("Given valid approval and request in cache when approve request then send to customer")
    @Test
    void approveLoanAmountRequestSuccess() throws Exception  {

        final String givenCustomerId = "xx-xxxx-xxx";
        final String givenManagerUsername = "bbb";
        final LoanRequest given = new LoanRequest();
        given.setCustomerId(givenCustomerId);
        given.setAmount(new BigDecimal(1));
        given.setManagerApprovals(Set.of(new ManagerApproval("aaa", true),
                new ManagerApproval("bbb", false)));

        requestsCache.add(given);

        final ManagerApprovalDto givenDto = new ManagerApprovalDto();
        givenDto.setCustomerId(givenCustomerId);
        givenDto.setUsername(givenManagerUsername);

        mockMvc.perform(MockMvcRequestBuilders.put(URL + "/approvals").content(toJson(givenDto))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(loanExternalService, times(1)).sendToCustomer(NotificationToCustomerDto.builder()
                .amount(new BigDecimal(1)).customerId(givenCustomerId).build());
        verifyNoMoreInteractions(loanExternalService);
    }

    @DisplayName("Given invalid approval when approve request then get bad request")
    @Test
    void approveLoanAmountRequestFail() throws Exception  {

        final ManagerApprovalDto givenDto = new ManagerApprovalDto();
        givenDto.setCustomerId("givenCustomerId");
        givenDto.setUsername(null);

        final String actual = mockMvc.perform(MockMvcRequestBuilders.put(URL + "/approvals").content(toJson(givenDto))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        assertThat(actual).contains("Managers username cannot be empty.")
                .contains("Customer id must match pattern 'XX-XXXX-XXX'.");
    }

    @DisplayName("Given logs when gather statistics then get statistics object")
    @Test
    void getStatisticsSuccess() throws Exception {

        final LocalDateTime givenTime = LocalDateTime.of(2020, 1, 1, 0, 0, 1);
        IntStream.range(0, 20).mapToObj(index -> LoanRequestLog.builder()
            .sentToCustomerTime(givenTime.plusMinutes(40)).amount(new BigDecimal(index)).build())
                .forEach(logsCache::add);

        final String actual = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/statistics")
                .param("periodStart", "2020-01-01 00:00:00")
                .param("periodEnd", "2020-01-02 00:00:00")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(actual).contains("\"count\":20").contains("\"sum\":190").contains("\"avg\":9.50")
            .contains("\"max\":19").contains("\"min\":0");

    }

    @DisplayName("Given bad format dates when gather stats then get bad request")
    @Test
    void getStatisticsFail() throws Exception {
        final String actual = mockMvc.perform(MockMvcRequestBuilders.get(URL + "/statistics")
                .param("periodStart", "badFormatStart")).andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(actual).contains("badFormatStart should match time format pattern");
    }

    private String toJson(final Object object) throws Exception {
        return OBJECT_MAPPER.writeValueAsString(object);
    }
}