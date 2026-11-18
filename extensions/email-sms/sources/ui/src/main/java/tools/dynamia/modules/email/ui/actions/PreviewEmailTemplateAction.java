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

import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.modules.email.api.EmailTemplatesApiService;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.ui.SandboxedFrame;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;

@InstallAction
public class PreviewEmailTemplateAction extends AbstractCrudAction {

    @Autowired
    private EmailTemplatesApiService previewService;

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
            // saved templates are rendered with sample data of the model providers; the frame cannot run scripts
            String html = template.getContent();
            String title = template.getSubject();
            if (template.getId() != null) {
                try {
                    var preview = previewService.preview(template.getId(), null, null, null, null);
                    html = preview.html();
                    title = preview.subject();
                } catch (RuntimeException e) {
                    // not rendered (for example a template of another account): show the content as it is
                }
            }
            var frame = new Div();
            frame.setVflex("1");
            frame.setHflex("1");
            frame.appendChild(new Html(SandboxedFrame.markup(html)));
            ZKUtil.showDialog(title, frame, "100%", "100%");
        } else {
            UIMessages.showMessage("Select template", MessageType.ERROR);
        }
    }
}
