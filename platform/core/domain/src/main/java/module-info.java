open module tools.dynamia.domain {
    requires tools.dynamia.commons;
    requires tools.dynamia.integration;
    requires jakarta.validation;

    requires com.fasterxml.jackson.databind;
    requires spring.context;
    requires java.sql;
    requires spring.jdbc;
    requires java.net.http;
    requires spring.tx;
    exports tools.dynamia.domain;
    exports tools.dynamia.domain.query;
    exports tools.dynamia.domain.util;
    exports tools.dynamia.domain.services;
    exports tools.dynamia.domain.services.impl;
    exports tools.dynamia.domain.fx;
    exports tools.dynamia.domain.contraints;
    exports tools.dynamia.domain.jdbc;
    exports tools.dynamia.domain.notifications;
}
