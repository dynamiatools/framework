package tools.dynamia.modules.security.ui.controllers;

import org.zkoss.zul.Messagebox;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.modules.security.domain.UserAccessToken;
import tools.dynamia.zk.crud.CrudController;

import java.util.Date;

public class UserAccessTokenController extends CrudController<UserAccessToken> {

    /**
     * This method is called before executing a database query.
     * It checks if the parameter "fechaVencimiento" is null, and if so, it sets it to true.
     */
    @Override
    protected void beforeQuery() {
        if (getParameter("expirationDate") == null) {
            setParemeter("expirationDate", QueryConditions.geqt(new Date()));
        }
    }

    @Override
    protected void beforeSave() {
        if (getEntity().getId() == null) {
            Messagebox.show("Token: <b>" + getEntity().getToken() + "</b> <br/>created");
        }
    }
}
