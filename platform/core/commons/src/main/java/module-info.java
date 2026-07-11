open module tools.dynamia.commons {
    exports tools.dynamia.commons;
    exports tools.dynamia.commons.reflect;
    exports tools.dynamia.commons.collect;
    exports tools.dynamia.commons.logger;
    exports tools.dynamia.commons.math;
    requires org.slf4j;
    requires java.desktop;
    requires java.sql;
    requires spring.beans;
    requires spring.core;
    requires org.jspecify;
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.xml;


}
