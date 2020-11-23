package org.jurijz.loanamountapproval.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jurijz.loanamountapproval.domain.dto.NotificationToCustomerDto;
import org.jurijz.loanamountapproval.domain.dto.NotificationToManagerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanExternalService {

    private final WebClient webClient;
    @Value("${loan.approval.external.managers.uri}")
    private final String managersUri;
    @Value("${loan.approval.external.customers.uri}")
    private final String customersUri;

    /*
     * Sends notification to manager, that there is request pending and needs managers attention.
     */
    public void sendToManager(final NotificationToManagerDto notificationDto) {
        log.info("Sending loan info to manager: {}", notificationDto);
        webClient.post()
                .uri(managersUri)
                .body(Mono.just(notificationDto), NotificationToManagerDto.class)
                .retrieve()
                .bodyToMono(Void.class);
    }

    /*
     * Sends notification to customer, that his loan request was approved.
     */
    public void sendToCustomer(final NotificationToCustomerDto notificationToCustomerDto) {
        log.info("Sending approved amount to customer: {}", notificationToCustomerDto);
        webClient.post()
                .uri(customersUri)
                .body(Mono.just(notificationToCustomerDto), NotificationToCustomerDto.class)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
