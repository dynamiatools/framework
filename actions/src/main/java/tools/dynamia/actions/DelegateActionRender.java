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

package tools.dynamia.actions;

import tools.dynamia.integration.Containers;

import java.util.Optional;

/**
 * {@link ActionRenderer} that delegate the action ui rendering to specific {@link ActionRenderProvider}
 */
public class DelegateActionRender implements ActionRenderer<Object> {

	private String providerName;
	private ActionRenderer defaultRenderer;

	public DelegateActionRender(String providerName) {
		super();
		this.providerName = providerName;
	}

	public DelegateActionRender(String providerName, ActionRenderer defaultRenderer) {
		super();
		this.providerName = providerName;
		this.defaultRenderer = defaultRenderer;
	}

	@Override
	public Object render(Action action, ActionEventBuilder actionEventBuilder) {

		Optional<ActionRenderProvider> provider = Containers.get()
				.findObjects(ActionRenderProvider.class, object -> object.getName().equals(providerName)).stream().findFirst();

		if (provider.isPresent()) {
			return provider.get().getActionRenderer().render(action, actionEventBuilder);
		} else if (defaultRenderer != null) {
			return defaultRenderer.render(action, actionEventBuilder);
		} else {
			throw new ActionRendererException("No delegate action renderer found with provider: " + providerName);
		}
	}

	public String getProviderName() {
		return providerName;
	}

}
