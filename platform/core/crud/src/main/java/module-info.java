open module tools.dynamia.crud {
    requires tools.dynamia.commons;
    requires tools.dynamia.actions;
    requires tools.dynamia.integration;
    requires tools.dynamia.navigation;
    requires tools.dynamia.viewers;
    requires tools.dynamia.domain;
    requires tools.dynamia.ui;
    requires tools.jackson.core;
    requires tools.jackson.databind;
    exports tools.dynamia.crud;
    exports tools.dynamia.crud.actions;
    exports tools.dynamia.crud.actions.remote;
    exports tools.dynamia.crud.cfg;
}
