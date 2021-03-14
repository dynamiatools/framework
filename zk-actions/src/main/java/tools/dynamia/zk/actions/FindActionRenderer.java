/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.zk.actions;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Textbox;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.commons.Messages;
import tools.dynamia.web.util.HttpUtils;

public class FindActionRenderer implements ActionRenderer {

	private String label = Messages.get(FindActionRenderer.class, "search_lbl");
	private String width = "180px";
	private String height;
	private boolean autoclean;
	private String zclass;
	private String sclass;
	private String style;
	private String startValue;
	private String mobileWidth = "100px";

	public FindActionRenderer() {

	}

	public FindActionRenderer(String label) {
		super();
		this.label = label;
	}

	public FindActionRenderer(String label, String width) {
		super();
		this.label = label;
		this.width = width;
	}

	public String getMobileWidth() {
		return mobileWidth;
	}

	public void setMobileWidth(String mobileWidth) {
		this.mobileWidth = mobileWidth;
	}

	@Override
	public Object render(final Action action, final ActionEventBuilder actionEventBuilder) {
		final Textbox search = new Textbox();

		search.setWidth(width);
		if (HttpUtils.isSmartphone()) {
			search.setWidth(mobileWidth);
		}
		search.setPlaceholder(label);

		if (height != null) {
			search.setHeight(height);
		}

		if (zclass != null) {
			search.setZclass(zclass);
		}

		if (sclass != null) {
			search.setSclass(sclass);
		}

		if (style != null) {
			search.setStyle(style);
		}

		if (action.getAttribute("sclass") != null) {
			search.setSclass((String) action.getAttribute("sclass"));
		}

		if (action.getAttribute("style") != null) {
			search.setStyle(search.getStyle() + "; " + action.getAttribute("style"));
		}

		search.setTooltiptext(Messages.get(FindActionRenderer.class, "search_tt"));
		if (action.getDescription() != null) {
			search.setTooltiptext(action.getDescription());
		}

		search.addEventListener(Events.ON_OK, event -> {
			fireEvent(action, actionEventBuilder, search, event);
			if (autoclean) {
				search.setValue("");
			}
		});
		search.setValue(startValue);
		if (startValue != null && !startValue.isEmpty()) {
			fireEvent(action, actionEventBuilder, search, new Event(Events.ON_OK, search));
		}
		return search;
	}

	private void fireEvent(final Action action, final ActionEventBuilder actionEventBuilder, final Textbox search,
			Event event) {
		ActionEvent evt = actionEventBuilder.buildActionEvent(event.getTarget(), null);
		evt.setData(search.getValue());
		action.actionPerformed(evt);
	}

	public String getStartValue() {
		return startValue;
	}

	public void setStartValue(String startValue) {
		this.startValue = startValue;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getSclass() {
		return sclass;
	}

	public void setSclass(String sclass) {
		this.sclass = sclass;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public boolean isAutoclean() {
		return autoclean;
	}

	public void setAutoclean(boolean autoclean) {
		this.autoclean = autoclean;
	}

	public String getZclass() {
		return zclass;
	}

	public void setZclass(String zclass) {
		this.zclass = zclass;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
