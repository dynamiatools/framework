package tools.dynamia.modules.saas.migration.services;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.saas.AccountTenants;
import tools.dynamia.modules.saas.migration.api.AccountCloneOptions;
import tools.dynamia.modules.saas.migration.api.AccountExportOptions;
import tools.dynamia.modules.saas.migration.api.AccountImportOptions;
import tools.dynamia.modules.saas.migration.pipeline.ExportPipeline;
import tools.dynamia.modules.saas.migration.pipeline.ImportPipeline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Account migration reads and writes the data of every account, so it always runs as the root tenant.
 */
class AccountMigrationServiceRootTenantTest {

    private final List<Long> tenantsSeen = new ArrayList<>();
    private final ExportPipeline export = mock(ExportPipeline.class);
    private final ImportPipeline importer = mock(ImportPipeline.class);
    private final AccountMigrationServiceImpl service = new AccountMigrationServiceImpl(export, importer);

    AccountMigrationServiceRootTenantTest() {
        doAnswer(inv -> tenantsSeen.add(AccountTenants.forcedTenantId())).when(export).export(any(), any(), any(), any(), any());
        doAnswer(inv -> tenantsSeen.add(AccountTenants.forcedTenantId())).when(importer).importTenant(any(), any(), any(), any());
    }

    @Test
    void exportRunsAsRoot() {
        service.exportTenant(5L, new ByteArrayOutputStream(), new AccountExportOptions(), null, null);

        assertEquals(List.of(AccountTenants.ROOT_TENANT_ID), tenantsSeen);
        assertNull(AccountTenants.forcedTenantId());
    }

    @Test
    void importRunsAsRoot() {
        service.importTenant(new ByteArrayInputStream(new byte[0]), new AccountImportOptions().targetAccountId(6L), null, null);

        assertEquals(List.of(AccountTenants.ROOT_TENANT_ID), tenantsSeen);
        assertNull(AccountTenants.forcedTenantId());
    }

    @Test
    void cloneRunsBothPhasesAsRoot() {
        service.cloneTenant(new AccountCloneOptions().source(5L).target(6L), null, null);

        assertEquals(List.of(AccountTenants.ROOT_TENANT_ID, AccountTenants.ROOT_TENANT_ID), tenantsSeen);
        assertNull(AccountTenants.forcedTenantId());
    }
}
