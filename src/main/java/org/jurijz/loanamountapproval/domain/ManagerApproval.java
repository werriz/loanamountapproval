package org.jurijz.loanamountapproval.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ManagerApproval {

    private String username;
    private boolean isApproved;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ManagerApproval that = (ManagerApproval) o;
        return Objects.equals(username, that.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
