open module tools.dynamia.io {
    requires spring.core;
    requires tools.dynamia.commons;
    requires spring.context;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires tools.dynamia.integration;
    exports tools.dynamia.io;
    exports tools.dynamia.io.converters;
    exports tools.dynamia.io.qr;
}
