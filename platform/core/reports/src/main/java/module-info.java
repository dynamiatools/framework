open module tools.dynamia.reports {
    requires tools.dynamia.integration;
    requires tools.dynamia.commons;
    requires tools.dynamia.domain;
    requires tools.dynamia.io;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.sql;
    requires spring.context;
    requires spring.beans;
    exports tools.dynamia.reports;
    exports tools.dynamia.reports.excel;
}
