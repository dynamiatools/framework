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

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class Report implements Serializable {

    private Object content;
    private String name;
    private Date date;
    private ReportOutputType defaultOutputType;

    /**
     *
     * @param content
     * @param name
     */
    public Report(Object content, String name, ReportOutputType defauOuputType) {
        this.content = content;
        this.name = name;
        this.date = new Date();
        this.defaultOutputType = defauOuputType;
    }

    public ReportOutputType getDefaultOutputType() {
        return defaultOutputType;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public Object getContent() {
        return content;
    }

    /**
     *
     * @return
     */
    public Date getDate() {
        return date;
    }
}
