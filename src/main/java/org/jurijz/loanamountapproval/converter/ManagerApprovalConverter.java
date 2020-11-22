package org.jurijz.loanamountapproval.converter;

import org.jurijz.loanamountapproval.domain.ManagerApproval;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ManagerApprovalConverter implements Converter<String, ManagerApproval> {

    @Override
    public ManagerApproval convert(final String source) {
        final ManagerApproval target = new ManagerApproval();
        target.setUsername(source);
        target.setApproved(false);
        return target;
    }

    public Set<ManagerApproval> convertAll(final Set<String> source) {
        return source.stream().map(this::convert).collect(Collectors.toSet());
    }
}
