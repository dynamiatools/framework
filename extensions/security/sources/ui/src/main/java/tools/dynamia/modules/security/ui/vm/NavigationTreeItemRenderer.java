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

package tools.dynamia.modules.security.ui.vm;

import org.zkoss.zhtml.I;
import org.zkoss.zhtml.Span;
import org.zkoss.zhtml.Text;
import org.zkoss.zul.*;
import tools.dynamia.navigation.NavigationElement;
import tools.dynamia.navigation.PageAction;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

public class NavigationTreeItemRenderer implements TreeitemRenderer<Object> {

    @Override
    public void render(Treeitem item, Object data, int index) {
        DefaultTreeNode node = (DefaultTreeNode) data;

        data = node.getData();

        item.setValue(data);

        Treerow row = new Treerow();
        item.appendChild(row);

        Treecell cell = new Treecell();
        row.appendChild(cell);

        String image = null;
        String label = data.toString();

        if (data instanceof NavigationElement nav) {
            image = nav.getIcon();
        }

        I icon = new I();
        ZKUtil.configureComponentIcon(image, icon, IconSize.SMALL);

        Span span = new Span();
        span.appendChild(icon);
        span.appendChild(new Text(" " + label));

        cell.appendChild(span);

    }

}
