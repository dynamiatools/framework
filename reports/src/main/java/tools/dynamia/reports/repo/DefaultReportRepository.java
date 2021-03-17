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
package tools.dynamia.reports.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.io.FileInfo;
import tools.dynamia.reports.ReportExplorer;
import tools.dynamia.reports.jr.JasperReportCompiler;
import tools.dynamia.reports.jr.JasperReportExplorerFilter;

import java.io.File;
import java.util.List;

@Service
public class DefaultReportRepository extends CrudServiceListenerAdapter<Parameter> implements ReportsRepository {

	private final LoggingService logger = new SLF4JLoggingService(getClass());

	private static final String PARAM_NAME = "GLOBAL_REPORTS_LOCATION";
	private String reportLocation;

	@Autowired
	private Parameters appParams;

	private final ReportExplorer explorer;

	public DefaultReportRepository() {
		explorer = new ReportExplorer(new JasperReportExplorerFilter(), new JasperReportCompiler());
	}

	public DefaultReportRepository(String reportLocation, ReportExplorer reportExplorer) {
		super();
		this.reportLocation = reportLocation;
		this.explorer = reportExplorer;
	}

	/**
	 * @param name
	 */
	@Override
	public FileInfo findReport(String name) {

		return explorer.find(new File(getReportLocation()), name);
	}

	@Override
	public FileInfo findReport(String subdirectory, String name) {
		return explorer.find(new File(getReportLocation() + "/" + subdirectory), name);
	}

	/**
	 *
	 * @param subdirectory
	 * @return
	 */
	@Override
	public List<FileInfo> scan(String subdirectory) {
		return explorer.scan(new File(getReportLocation() + "/" + subdirectory));
	}

	@Override
	public List<FileInfo> scan() {
		return explorer.scan(new File(getReportLocation()));
	}

	@Override
	public String getReportLocation() {
		if (reportLocation == null) {
			reportLocation = appParams.getValue(PARAM_NAME);
			if (reportLocation == null) {
				reportLocation = new File(".").getAbsolutePath();
				logger.warn("Reports Location not configured. Using default location: " + reportLocation);
			}
		}
		return reportLocation;
	}

	@Override
	public void afterCreate(Parameter entity) {
		resetReportLocation(entity);
	}

	@Override
	public void afterUpdate(Parameter entity) {
		resetReportLocation(entity);
	}

	private void resetReportLocation(Parameter entity) {
		if (entity.getName().equals(PARAM_NAME)) {
			reportLocation = null;
		}
	}

}
