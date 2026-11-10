open module tools.dynamia.templates {
    requires tools.dynamia.integration;
    requires java.desktop; // java.beans.Introspector for template property paths
    exports tools.dynamia.templates;
    uses tools.dynamia.templates.TemplateFilter;
}
