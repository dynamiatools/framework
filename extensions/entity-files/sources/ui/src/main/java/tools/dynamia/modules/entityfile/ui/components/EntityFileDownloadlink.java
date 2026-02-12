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

package tools.dynamia.modules.entityfile.ui.components;

import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Toolbarbutton;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.ui.util.EntityFileUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.io.Serial;

public class EntityFileDownloadlink extends Toolbarbutton {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -2182747459195865750L;

    static {
        BindingComponentIndex.getInstance().put("value", EntityFileDownloadlink.class);
        ComponentAliasIndex.getInstance().put("entityfileDownloadlink", EntityFileDownloadlink.class);
    }

    private EntityFile entityFile;
    private boolean showLabel = true;

    public EntityFileDownloadlink() {
        setIconSclass("z-icon-paperclip");
        addEventListener(Events.ON_CLICK, event -> {
            if (entityFile != null) {
                StoredEntityFile sef = entityFile.getStoredEntityFile();
                if (sef.getRealFile() != null && sef.getRealFile().exists()) {
                    Media media = new AMedia(entityFile.getName(), entityFile.getExtension(), null, sef.getRealFile(), null);
                    Filedownload.save(media, entityFile.getName());
                } else {
                    EntityFileUtils.showDownloadDialog(entityFile.getStoredEntityFile());
                }
            }else{
                Clients.showNotification("No file",this);
            }
        });
    }

    public EntityFile getValue() {
        return entityFile;
    }

    public void setValue(EntityFile entityFile) {
        this.entityFile = entityFile;
        rendererLabel();
    }

    private void rendererLabel() {
        if(entityFile!=null) {
            if (showLabel) {
                setLabel(entityFile.getName());
            }
            setTooltiptext(entityFile.getName());
        }
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }
}
