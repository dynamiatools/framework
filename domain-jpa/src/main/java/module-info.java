open module tools.dynamia.domain.jpa {
    requires jakarta.persistence;
    requires tools.dynamia.commons;
    requires tools.dynamia.domain;
    requires com.fasterxml.jackson.annotation;
    requires spring.jdbc;
    requires spring.context;
    requires spring.orm;
    requires spring.tx;
    requires java.naming;
    requires tools.dynamia.integration;
    requires tools.dynamia.io;
    requires jakarta.annotation;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires jakarta.validation;
    requires org.hibernate.orm.core;
    exports tools.dynamia.domain.jpa;

}
