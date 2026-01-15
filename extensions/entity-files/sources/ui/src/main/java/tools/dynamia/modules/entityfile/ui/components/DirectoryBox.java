
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

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;

import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.io.Serial;

/**
 *
 * @author Mario Serrano Leones
 */
public class DirectoryBox extends Bandbox {

    /**
	 *
	 */
	@Serial
    private static final long serialVersionUID = -7769832324226733919L;

	static {
        BindingComponentIndex.getInstance().put("value", DirectoryBox.class);
        ComponentAliasIndex.getInstance().add(DirectoryBox.class);
    }

    public DirectoryBox() {
        this(null);
    }

    public DirectoryBox(String value) throws WrongValueException {
        super(value);
        DirectoryExplorer explorer = new DirectoryExplorer();
        Bandpopup popup = new Bandpopup();
        popup.appendChild(explorer);

        popup.setWidth("400px");
        popup.setHeight("300px");

        appendChild(popup);

        explorer.addEventListener(Events.ON_CHANGE, event -> {
		    setValue(event.getData().toString());
		    Events.postEvent(new Event(Events.ON_CHANGE, DirectoryBox.this, getValue()));
		});

    }



}
