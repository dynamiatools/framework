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
package tools.dynamia.zk.reports.actions;

import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.reports.ExporterColumn;
import tools.dynamia.reports.PlainFileExporter;
import tools.dynamia.reports.ReportOutputType;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.ui.LongOperationMonitorWindow;
import tools.dynamia.zk.util.LongOperation;

import java.io.File;
import java.util.Collection;

@InstallAction
public class ExportCSVAction extends AbstractExportAction {



	public ExportCSVAction() {
		setName(Messages.get(getClass(), "export_csv"));

	}

	@Override
	public ReportOutputType getOuputType() {
		return ReportOutputType.CSV;
	}


	public void export(Collection data, ViewDescriptor descriptor) {

		PlainFileExporter exporter = new PlainFileExporter();

		Viewers.getFields(descriptor).stream().filter(f -> f.isVisible() && !f.isCollection()).forEach(f -> {
			ExporterColumn column = new ExporterColumn(f.getName(), f.getLocalizedLabel(), getFormatPattern(f),
					f.getFieldClass());
			if (f.getParams().get("entityAlias") != null) {
				column.setEntityAlias((String) f.getParams().get("entityAlias"));
			}
			//noinspection unchecked
			exporter.addColumn(column);
		});

		File temp = createTempFile();

		ProgressMonitor monitor = new ProgressMonitor();

		@SuppressWarnings("unchecked") LongOperation operation = LongOperation.create().execute(() -> exporter.export(temp, data, monitor))
				.onFinish(() -> download(temp)).onException(Throwable::printStackTrace).start();

		// Update ui each 4 secs
		LongOperationMonitorWindow monitorWindow = new LongOperationMonitorWindow(operation, monitor);
		monitorWindow.setMessageTemplate(MESSAGES.get("exporting_progress"));
		monitorWindow.setTitle(MESSAGES.get("exporting"));
		monitorWindow.doModal();
	}

	private static String getFormatPattern(Field f) {
		String formatPattern = (String) f.getParams().get(Viewers.PARAM_FORMAT_PATTERN);
		if (formatPattern == null || formatPattern.isEmpty()) {
			String converter = (String) f.getParams().get(Viewers.PARAM_CONVERTER);
			if (converters.Date.class.getName().equals(converter)) {
				formatPattern = "dd/MM/yyyy";
			} else if (converters.Time.class.getName().equals(converter)) {
				formatPattern = "h:mm a";
			}
		}
		return formatPattern;
	}

}
