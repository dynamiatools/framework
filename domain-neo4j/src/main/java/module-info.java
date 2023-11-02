open module tools.dynamia.domain.neo4j {
    requires tools.dynamia.integration;
    requires tools.dynamia.domain;
    requires org.neo4j.ogm.core;
    requires spring.tx;
    requires tools.dynamia.commons;
    requires org.neo4j.ogm.drivers.api;
    requires tools.dynamia.io;
    exports tools.dynamia.domain.neo4j;
}
