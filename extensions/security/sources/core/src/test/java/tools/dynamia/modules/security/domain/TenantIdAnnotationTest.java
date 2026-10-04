package tools.dynamia.modules.security.domain;

import org.hibernate.annotations.TenantId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards that every security entity that owns its {@code accountId} declares it as the Hibernate tenant discriminator.
 */
class TenantIdAnnotationTest {

    @Test
    void securityEntitiesDeclareTheTenantId() throws Exception {
        for (Class<?> type : new Class<?>[]{User.class, Profile.class, UserProfile.class, Permission.class, UserAccessToken.class}) {
            assertTrue(type.getDeclaredField("accountId").isAnnotationPresent(TenantId.class), type.getSimpleName());
        }
    }
}
