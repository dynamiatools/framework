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

import tools.dynamia.commons.ObjectOperations;
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

    private String preferedCompiler = "jasperreports";

    public SimpleReportDescriptor() {

    }

    /**
     *
     */
    public SimpleReportDescriptor(Object reportTemplate) {
        this(reportTemplate, null, new HashMap<>());
    }

    /**
     *
     */
    public SimpleReportDescriptor(Object reportTemplate, Object dataSource) {
        this(reportTemplate, dataSource, new HashMap<>());
    }

    /**
     *
     */
    public SimpleReportDescriptor(Object reportTemplate, Object dataSource, Map<String, Object> parameters) {
        this("SimpleReport", reportTemplate, dataSource, parameters, ReportOutputType.PDF);
    }

    /**
     *
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
     *
     */
    @Override
    public Object getTemplate() {
        return reportTemplate;
    }

    public void setTemplate(Object reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    /**
     *
     */
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     *
     */
    @Override
    public Object getDataSource() {
        return dataSource;
    }

    public void setDataSource(Object dataSource) {
        this.dataSource = dataSource;
    }

    /**
     *
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
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
        //noinspection unchecked
        getExporterParameters().put(name, value);
    }

    public void buildParameters(String prefix, Object object) {
        if (object == null) {
            return;
        }
        getParameters().putAll(ObjectOperations.getValuesMaps(prefix, object));
    }

    public void loadParameterFromProviders(Object object) {
        Containers.get().findObjects(ReportParametersProvider.class).forEach(p -> {
            try {
                @SuppressWarnings("unchecked") Map<String, Object> params = p.getParams(object);
                if (params != null) {
                    getParameters().putAll(params);
                }
            } catch (ClassCastException e) {
                // ignore
            }
        });
    }

    @Override
    public String getPreferedCompiler() {
        return preferedCompiler;
    }

    public void setPreferedCompiler(String preferedCompiler) {
        this.preferedCompiler = preferedCompiler;
    }
}
