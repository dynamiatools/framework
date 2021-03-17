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

package tools.dynamia.reports;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.integration.ObjectMatcher;
import tools.dynamia.integration.ProgressMonitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class PlainFileExporter<T> {

	private String encoding = "UTF-8";
	private String separator = "\t";
	private String lineSeparator = "\n";
	private final List<ExporterColumn<T>> columns = new ArrayList<>();
	private ObjectMatcher<T> matcher;

	public void addColumn(String name, String title) {
		columns.add(new ExporterColumn<>(name, title));
	}

	public void addColumn(String name, String title, ExporterFieldLoader<T> loader) {
		ExporterColumn<T> column = new ExporterColumn<>(name, title);
		columns.add(column);
	}

	public void addColumn(String name) {
		addColumn(name, name);
	}

	public void addColumn(ExporterColumn<T> column) {
		columns.add(column);
	}

	public void setMatcher(ObjectMatcher<T> matcher) {
		this.matcher = matcher;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void export(File outputFile, Collection<T> data, ProgressMonitor monitor) {

		int line = 0;
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputFile), encoding))) {

			if (monitor != null) {
				monitor.setMax(data.size());
			}

			for (ExporterColumn<T> column : columns) {
				writer.append(column.getTitle()).append(separator);
			}

			writer.append(lineSeparator);

			for (T bean : data) {
				if (matcher == null || matcher.match(bean)) {
					for (ExporterColumn<T> col : columns) {
						Object value = " ";
						try {
							if (col.getFieldLoader() != null) {
								value = col.getFieldLoader().load(col.getName(), bean);
							} else {
								if (col.getColumnClass() != null && col.getColumnClass().equals(boolean.class)) {
									value = BeanUtils.invokeBooleanGetMethod(bean, col.getName());
								} else {
									value = BeanUtils.invokeGetMethod(bean, col.getName());

									value = ExporterUtils.checkAndLoadEntityReferenceValue(col, value);
								}
							}
						} catch (Exception e) {
							System.err.printf("PFE: Error invoking get method %s in bean %s \n", col.getName(), bean);
						}
						
						String formatPattern = col.getFormatPattern();
						if (formatPattern != null && !formatPattern.isEmpty()) {
							if (value instanceof Date) {
								value = new SimpleDateFormat(formatPattern).format(value);
							} else if (value instanceof Number) {
								value = new DecimalFormat(formatPattern).format(value);
							}
						}
						
						if (value == null || value.equals("null")) {
							value = " ";
						}
						

						writer.append(value.toString()).append(separator);
					}
					writer.append(lineSeparator);
					line++;
					if (monitor != null) {
						monitor.setCurrent(line);
						if (monitor.isStopped()) {
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);

		}
	}

}
