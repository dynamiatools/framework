package tools.dynamia.modules.saas;

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.modules.saas.domain.Account;

import static tools.dynamia.commons.DateTimeUtils.createDate;

public class AccountTest {

    @Test
    public void testTrialPeriod() {
        final int TRIAL = 15;
        var creation = createDate(2022, 1, 1);

        var account = new Account();
        account.setCreationDate(creation);
        account.setFreeTrial(TRIAL);

        int left = account.computeTrialLeft(account.getFreeTrial(), creation);
        System.out.println("Trial Left 0 = " + left);
        Assert.assertEquals(TRIAL, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createDate(2022, 1, 5));
        System.out.println("Trial Left 1 = " + left);
        Assert.assertEquals(10 + 1, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createDate(2022, 1, 10));
        System.out.println("Trial Left 2 = " + left);
        Assert.assertEquals(5 + 1, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createDate(2022, 1, 15));
        System.out.println("Trial Left 3 = " + left);
        Assert.assertEquals(1, left);

        left = account.computeTrialLeft(account.getFreeTrial(), createDate(2022, 1, 20));
        System.out.println("Trial Left 4 = " + left);
        Assert.assertEquals(0, left);

        Assert.assertFalse(account.isInFreeTrial());
    }
}
