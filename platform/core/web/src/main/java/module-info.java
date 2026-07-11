open module tools.dynamia.web {
    requires tools.dynamia.navigation;
    requires tools.dynamia.integration;
    requires tools.dynamia.commons;
    requires tools.dynamia.crud;
    requires tools.dynamia.domain;
    requires tools.dynamia.viewers;
    requires jakarta.servlet;
    requires spring.webmvc;
    requires spring.web;
    requires spring.context;
    requires spring.beans;
    requires spring.core;


    requires java.net.http;
    requires java.scripting;
    requires jakarta.validation;
    requires io.swagger.v3.oas.annotations;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.databind;
    exports tools.dynamia.web.navigation;
    exports tools.dynamia.web.util;
}
