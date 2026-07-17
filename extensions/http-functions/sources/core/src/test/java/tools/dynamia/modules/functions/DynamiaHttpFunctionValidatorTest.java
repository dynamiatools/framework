package tools.dynamia.modules.functions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;
import tools.dynamia.modules.functions.domain.enums.FunctionStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the versioning and required-field rules enforced by {@link DynamiaHttpFunctionValidator}.
 * {@link CrudService} is mocked and installed into {@link Containers} so the validator's static
 * {@code DomainUtils.lookupCrudService()} lookup resolves to the mock instead of a real datasource.
 */
class DynamiaHttpFunctionValidatorTest {

    private final DynamiaHttpFunctionValidator validator = new DynamiaHttpFunctionValidator();
    private CrudService crudService;

    @BeforeEach
    void installMockCrudService() {
        crudService = mock(CrudService.class);
        SimpleObjectContainer container = new SimpleObjectContainer("test-container");
        container.addObject(crudService);
        Containers.get().installObjectContainer(container);
    }

    @AfterEach
    void resetContainers() {
        Containers.get().removeAllContainers();
    }

    @Test
    void requiresName() {
        DynamiaHttpFunction function = draftFunction(null, 1);

        assertThrows(ValidationError.class, () -> validator.validate(function));
    }

    @Test
    void doesNotRequireUrlWhileDraft() {
        DynamiaHttpFunction function = draftFunction("WhatsApp.sendMessage", 1);
        function.setUrl(null);
        noExistingVersions();

        assertDoesNotThrow(() -> validator.validate(function));
    }

    @Test
    void requiresUrlWhenActive() {
        DynamiaHttpFunction function = draftFunction("WhatsApp.sendMessage", 1);
        function.setUrl(null);
        function.setStatus(FunctionStatus.ACTIVE);
        noExistingVersions();

        assertThrows(ValidationError.class, () -> validator.validate(function));
    }

    @Test
    void versionMustStartAtOne() {
        DynamiaHttpFunction function = draftFunction("WhatsApp.sendMessage", 0);

        assertThrows(ValidationError.class, () -> validator.validate(function));
    }

    @Test
    void rejectsDuplicateNameAndVersion() {
        DynamiaHttpFunction function = draftFunction("WhatsApp.sendMessage", 1);
        when(crudService.count(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(1L);

        assertThrows(ValidationError.class, () -> validator.validate(function));
    }

    @Test
    void newVersionMustBeGreaterThanCurrentMax() {
        DynamiaHttpFunction function = draftFunction("WhatsApp.sendMessage", 2);
        when(crudService.count(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(0L);

        DynamiaHttpFunction existingV2 = draftFunction("WhatsApp.sendMessage", 2);
        when(crudService.find(eq(DynamiaHttpFunction.class), any(QueryParameters.class)))
                .thenReturn(List.of(existingV2));

        assertThrows(ValidationError.class, () -> validator.validate(function));
    }

    @Test
    void acceptsNewVersionGreaterThanCurrentMax() {
        DynamiaHttpFunction function = draftFunction("WhatsApp.sendMessage", 3);
        when(crudService.count(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(0L);

        DynamiaHttpFunction existingV2 = draftFunction("WhatsApp.sendMessage", 2);
        when(crudService.find(eq(DynamiaHttpFunction.class), any(QueryParameters.class)))
                .thenReturn(List.of(existingV2));

        assertDoesNotThrow(() -> validator.validate(function));
    }

    @Test
    void skipsUniquenessAndVersionChecksWhenUpdatingExistingFunction() {
        DynamiaHttpFunction function = draftFunction("WhatsApp.sendMessage", 1);
        function.setId(10L);

        assertDoesNotThrow(() -> validator.validate(function));
    }

    private void noExistingVersions() {
        when(crudService.count(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(0L);
        when(crudService.find(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(List.of());
    }

    private DynamiaHttpFunction draftFunction(String name, int version) {
        DynamiaHttpFunction function = new DynamiaHttpFunction();
        function.setName(name);
        function.setFunctionVersion(version);
        function.setUrl("https://api.example.com/send");
        function.setStatus(FunctionStatus.DRAFT);
        return function;
    }
}
