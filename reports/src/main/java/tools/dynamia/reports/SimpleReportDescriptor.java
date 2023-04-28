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
package tools.dynamia.reports;

import net.sf.jasperreports.engine.JREmptyDataSource;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.integration.Containers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class SimpleReportDescriptor implements ReportDescriptor {

    private String name;
    private Object reportTemplate;
    private Object dataSource;
    private Map<String, Object> parameters = new HashMap<>();
    private final Map exporterParameters = new HashMap();
    private ReportOutputType defaultOutputType;

    public SimpleReportDescriptor() {

    }

    /**
     * @param reportTemplate
     */
    public SimpleReportDescriptor(Object reportTemplate) {
        this(reportTemplate, new JREmptyDataSource(), new HashMap<>());
    }

    /**
     * @param reportTemplate
     * @param dataSource
     */
    public SimpleReportDescriptor(Object reportTemplate, Object dataSource) {
        this(reportTemplate, dataSource, new HashMap<>());
    }

    /**
     * @param reportTemplate
     * @param dataSource
     * @param parameters
     */
    public SimpleReportDescriptor(Object reportTemplate, Object dataSource, Map<String, Object> parameters) {
        this("SimpleReport", reportTemplate, dataSource, parameters, ReportOutputType.PDF);
    }

    /**
     * @param name
     * @param reportTemplate
     * @param dataSource
     * @param parameters
     * @param defaultOutputType
     */
    public SimpleReportDescriptor(String name, Object reportTemplate, Object dataSource, Map<String, Object> parameters,
                                  ReportOutputType defaultOutputType) {
        this.name = name;
        this.reportTemplate = reportTemplate;
        this.dataSource = dataSource instanceof ReportDataSource ? ((ReportDataSource) dataSource).getValue() : dataSource;
        this.parameters = parameters;
        this.defaultOutputType = defaultOutputType;
    }

    /**
     * @return
     */
    @Override
    public Object getTemplate() {
        return reportTemplate;
    }

    public void setTemplate(Object reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    /**
     * @return
     */
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * @return
     */
    @Override
    public Object getDataSource() {
        if (dataSource == null) {
            dataSource = new JREmptyDataSource();
        }
        return dataSource;
    }

    public void setDataSource(Object dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    @Override
    public ReportOutputType getDefaultOutputType() {
        return defaultOutputType;
    }

    public void setDefaultOutputType(ReportOutputType defaultOutputType) {
        this.defaultOutputType = defaultOutputType;
    }

    @Override
    public Map getExporterParameters() {
        return exporterParameters;
    }

    public void addParameter(String name, Object value) {
        getParameters().put(name, value);
    }

    public void addExporterParameter(Object name, Object value) {
        getExporterParameters().put(name, value);
    }

    public void buildParameters(String prefix, Object object) {
        if (object == null) {
            return;
        }
        getParameters().putAll(BeanUtils.getValuesMaps(prefix, object));
    }

    public void loadParameterFromProviders(Object object) {
        Containers.get().findObjects(ReportParametersProvider.class).forEach(p -> {
            try {
                Map<String, Object> params = p.getParams(object);
                if (params != null) {
                    getParameters().putAll(params);
                }
            } catch (ClassCastException e) {
                // ignore
            }
        });
    }

}
