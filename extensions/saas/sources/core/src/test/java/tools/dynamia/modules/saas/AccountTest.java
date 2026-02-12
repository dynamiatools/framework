package tools.dynamia.modules.saas;

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.modules.saas.domain.Account;

import java.time.LocalDateTime;

import static tools.dynamia.commons.DateTimeUtils.createLocalDate;

public class AccountTest {

    @Test
    public void testTrialPeriod() {
        final int TRIAL = 15;
        var creation = LocalDateTime.of(2022, 1, 1, 0, 0);


        var account = new Account();
        account.setCreationDate(creation);
        account.setFreeTrial(TRIAL);

        int left = account.computeTrialLeft(account.getFreeTrial(), creation.toLocalDate());
        System.out.println("Trial Left 0 = " + left);
        Assert.assertEquals(TRIAL, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createLocalDate(2022, 1, 5));
        System.out.println("Trial Left 1 = " + left);
        Assert.assertEquals(10 + 1, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createLocalDate(2022, 1, 10));
        System.out.println("Trial Left 2 = " + left);
        Assert.assertEquals(5 + 1, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createLocalDate(2022, 1, 15));
        System.out.println("Trial Left 3 = " + left);
        Assert.assertEquals(1, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createLocalDate(2022, 1, 20));
        System.out.println("Trial Left 4 = " + left);
        Assert.assertEquals(0, left);

        Assert.assertFalse(account.isInFreeTrial());
    }
}
