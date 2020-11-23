package org.jurijz.loanamountapproval.domain;

import lombok.*;

@EqualsAndHashCode(exclude = "isApproved")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerApproval {

    private String username;
    private boolean isApproved;

}
