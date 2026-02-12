
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

package tools.dynamia.modules.importer;

import tools.dynamia.actions.AbstractAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.modules.importer.ui.Importer;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;

/**
 *
 * @author Mario Serrano Leones
 */
public abstract class ImportAction extends AbstractAction {

	private ProgressMonitor monitor;
	private boolean procesable = true;

	@Override
	public void actionPerformed(ActionEvent evt) {
		Importer win = (Importer) evt.getSource();
		actionPerformed(win);
		if (isProcesable()) {
			win.setCurrentAction(this);
		}
	}

	public abstract void actionPerformed(Importer importer);

	public abstract void processImportedData(Importer importer);

	@Override
	public ActionRenderer getRenderer() {
		return new ToolbarbuttonActionRenderer(true);
	}

	public void setMonitor(ProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public ProgressMonitor getMonitor() {
		return monitor;
	}

	public boolean isProcesable() {
		return procesable;
	}

}
