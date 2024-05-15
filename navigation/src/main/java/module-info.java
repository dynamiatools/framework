open module tools.dynamia.navigation {
    requires spring.context;
    requires tools.dynamia.actions;
    requires tools.dynamia.commons;
    requires tools.dynamia.integration;
    requires spring.beans;
    requires jakarta.annotation;
    requires com.fasterxml.jackson.annotation;
    exports tools.dynamia.navigation;
    exports tools.dynamia.navigation.restrictions;

}
