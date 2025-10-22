open module tools.dynamia.viewers {
    exports tools.dynamia.viewers;
    exports tools.dynamia.viewers.util;
    exports tools.dynamia.viewers.impl;
    requires tools.dynamia.commons;
    requires tools.dynamia.domain;
    requires com.fasterxml.jackson.annotation;
    requires jakarta.validation;
    requires tools.dynamia.integration;
    requires tools.dynamia.io;
    requires com.fasterxml.jackson.databind;
    requires spring.beans;
    requires org.yaml.snakeyaml;
    requires spring.expression;
    requires tools.dynamia.actions;
}
