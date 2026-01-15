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

package tools.dynamia.modules.email.ui.actions;

import org.zkoss.util.media.AMedia;
import org.zkoss.zul.Iframe;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;

@InstallAction
public class PreviewEmailTemplateAction extends AbstractCrudAction {

    public PreviewEmailTemplateAction() {
        setName("Preview");
        setImage("eye");
        setApplicableClass(EmailTemplate.class);
        setMenuSupported(true);
        setApplicableStates(CrudState.get(CrudState.READ, CrudState.UPDATE, CrudState.CREATE));
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        EmailTemplate template = (EmailTemplate) evt.getData();
        if (template != null) {
            Iframe iframe = new Iframe();
            iframe.setContent(new AMedia(template.getName() + ".html", "html", "text/html", template.getContent()));
            iframe.setVflex("1");
            iframe.setHflex("1");
            ZKUtil.showDialog(template.getSubject(), iframe, "100%", "100%");
        } else {
            UIMessages.showMessage("Select template", MessageType.ERROR);
        }
    }
}
