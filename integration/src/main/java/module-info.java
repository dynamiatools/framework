open module tools.dynamia.integration {
    requires tools.dynamia.commons;
    requires spring.context;
    requires spring.expression;
    requires spring.core;
    requires spring.tx;
    requires spring.beans;
    exports tools.dynamia.integration;
    exports tools.dynamia.integration.ms;
    exports tools.dynamia.integration.scheduling;
    exports tools.dynamia.integration.sterotypes;
    exports tools.dynamia.integration.search;
}
