/*
 * Copyright (c) 2009 - 2021 Dynamia Soluciones IT SAS  All Rights Reserved
 *
 * Todos los Derechos Reservados  2009 - 2021
 *
 * Este archivo es propiedad de Dynamia Soluciones IT NIT 900302344-1 en Colombia / Sur America,
 * esta estrictamente prohibida su copia o distribución sin previa autorización del propietario.
 * Puede contactarnos a info@dynamiasoluciones.com o visitar nuestro sitio web
 * https://www.dynamiasoluciones.com
 *
 * Autor: Ing. Mario Serrano Leones <mario@dynamiasoluciones.com>
 */

package tools.dynamia.modules.security.listeners;

import org.springframework.beans.factory.support.ScopeNotActiveException;
import tools.dynamia.domain.Auditable;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.security.CurrentUser;

@Listener
public class AuditableSecurityCrudServiceListener extends CrudServiceListenerAdapter<Auditable> {

    @Override
    public void beforeCreate(Auditable ent) {
        try {
            if (ent.getCreator() == null || ent.getCreator().isEmpty()) {
                ent.setCreator(CurrentUser.get().getUsername());
            }
        } catch (ScopeNotActiveException e) {
            //ignore
        }
    }

    @Override
    public void beforeUpdate(Auditable entity) {
        entity.setLastUpdater(CurrentUser.get().getUsername());
    }

}
