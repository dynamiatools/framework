package tools.dynamia.domain;

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.domain.contraints.EmailValidator;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmailValidatorTest {

    List<String> validEmails = List.of(
            "email+address@domain.com.co",
            "email+address+long+ago@domain.com.co",
            "dynamia@mydomain.co",
            "super-mario-bros@gmail.com",
            "somename@api-email.com",
            "mi-empresa@laempresa.com",
            "hi@jd1.com.co.gov",
            "123email@domain.com",
            "ThisIsAnEmail@DOMAIN.dot.com"
    );

    List<String> invalidEmails = List.of(
            "some@gmal.com.",
            "myemail.@.com",
            "somethis",
            ".wie#rd@email@gmail.com",
            "#$-123email@gmail.com",
            "3@a"
    );


    @Test
    public void shouldBeValidEmailAddress() {
        var validator = new EmailValidator();
        validEmails.forEach(email -> {
            System.out.println("Testing valid email " + email);
            assertTrue(validator.isValid(email));
        });
    }

    @Test
    public void shouldBeInvalidEmailAddress() {
        var validator = new EmailValidator();
        invalidEmails.forEach(email -> {
            System.out.println("Testing invalid email " + email);
            assertFalse(validator.isValid(email));
        });
    }
}
