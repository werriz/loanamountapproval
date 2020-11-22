package org.jurijz.loanamountapproval.converter;

import lombok.RequiredArgsConstructor;
import org.jurijz.loanamountapproval.domain.LoanRequest;
import org.jurijz.loanamountapproval.domain.dto.LoanRequestDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanRequestConverter implements Converter<LoanRequestDto, LoanRequest> {

    private final ManagerApprovalConverter managerApprovalConverter;

    @Override
    public LoanRequest convert(final LoanRequestDto source) {
        final LoanRequest target = new LoanRequest();
        target.setCustomerId(source.getCustomerId());
        target.setAmount(source.getAmount());
        target.setManagerApprovals(managerApprovalConverter.convertAll(source.getApprovers()));
        return target;
    }

}
