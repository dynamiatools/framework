package tools.dynamia.modules.saas.tenancy;

import org.hibernate.annotations.TenantId;
import org.junit.jupiter.api.Test;
import tools.dynamia.modules.saas.jpa.AccountParameter;
import tools.dynamia.modules.saas.jpa.BaseEntitySaaS;
import tools.dynamia.modules.saas.jpa.BaseEntityUuidSaaS;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;
import tools.dynamia.modules.saas.jpa.SimpleEntityUuidSaaS;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards that every SaaS entity that owns its {@code accountId} declares it as the Hibernate tenant discriminator.
 */
class TenantIdAnnotationTest {

    @Test
    void saasBaseEntitiesAndAccountParameterDeclareTheTenantId() throws Exception {
        for (Class<?> type : new Class<?>[]{BaseEntitySaaS.class, SimpleEntitySaaS.class, BaseEntityUuidSaaS.class,
                SimpleEntityUuidSaaS.class, AccountParameter.class}) {
            assertTrue(type.getDeclaredField("accountId").isAnnotationPresent(TenantId.class), type.getSimpleName());
        }
    }
}
