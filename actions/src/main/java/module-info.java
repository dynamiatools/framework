open module tools.dynamia.actions {
    requires tools.dynamia.commons;
    requires tools.dynamia.integration;
    requires spring.context;
    requires spring.tx;
    requires com.fasterxml.jackson.annotation;
    exports tools.dynamia.actions;
}
