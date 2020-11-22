package org.jurijz.loanamountapproval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = LoanAmountApprovalApplication.class)
class ContextIT {

    @Test
    void contextTest() {
        assertThat(".").isNotEqualTo("!");
    }

}
