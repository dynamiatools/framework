package tools.dynamia.modules.security;

import org.springframework.stereotype.Component;

@Component
public class ErrorIgnoringAntMatcher implements IgnoringSecurityMatcher {
    @Override
    public String[] matchers() {
        return new String[]{"/error", "/errors"};
    }
}
