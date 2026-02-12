/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.modules.entityfile.ui.util;

import org.zkoss.util.media.Media;
import org.zkoss.zul.A;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import tools.dynamia.io.IOUtils;
import tools.dynamia.modules.entityfile.EntityFileException;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.ui.EntityFileController;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.util.ZKUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class EntityFileUtils {

    public static UploadedFileInfo build(Media media) {
        try {
            File tmpFile = File.createTempFile("entityfiles-", media.getName());
            InputStream is = null;
            if (media.isBinary()) {
                is = media.getStreamData();
            } else {
                is = new ByteArrayInputStream(media.getStringData().getBytes());
            }
            IOUtils.copy(is, tmpFile);
            UploadedFileInfo info = new UploadedFileInfo(tmpFile);
            info.setFullName(media.getName());
            info.setLength(tmpFile.length());

            return info;
        } catch (Exception e) {
            throw new EntityFileException("Unable to upload file " + media.getName(), e);
        }
    }

    public static void showFileExplorer(Object obj) {
        if (obj != null) {

            CrudView view = (CrudView) Viewers.getView(EntityFile.class, "crud", null);

            view.setHeight(null);
            EntityFileController controller = (EntityFileController) view.getController();
            controller.setTargetEntity(obj);
            controller.doQuery();
            ZKUtil.showDialog("Archivos Asociados - " + obj, view, "85%", "80%");

        } else {
            UIMessages.showMessage("Debe seleccionar un elemento para ver los archivos asociados", MessageType.INFO);
        }
    }

    public static void showDownloadDialog(StoredEntityFile sef) {
        Vlayout vlayout = new Vlayout();
        vlayout.setStyle("text-align: center; background: white !important; margin: 10px");
        vlayout.appendChild(new Label(sef.getEntityFile().getName()));
        vlayout.appendChild(new Label(sef.getEntityFile().getDescription()));

        A downloadLink = new A("Descargar ");
        downloadLink.setHref(sef.getUrl());
        downloadLink.setTarget("_blank");
        downloadLink.setZclass("btn btn-primary");
        downloadLink.setIconSclass("fa fa-arrow-down");
        downloadLink.setStyle("margin: 20px");
        vlayout.appendChild(downloadLink);

        Window window = ZKUtil.showDialog("Descargar Archivo", vlayout, "400px", null);
        window.setStyle("background: white !important");
        window.setContentStyle("background: white !important");

        UIMessages.showMessage("Clic en el link para descargar  " + sef.getEntityFile().getName());

    }

}
