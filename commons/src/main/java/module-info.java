open module tools.dynamia.commons {
    exports tools.dynamia.commons;
    exports tools.dynamia.commons.reflect;
    exports tools.dynamia.commons.collect;
    exports tools.dynamia.commons.logger;
    exports tools.dynamia.commons.math;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;

    requires org.slf4j;
    requires java.desktop;
    requires java.sql;


}
