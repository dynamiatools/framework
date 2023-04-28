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

/**
 *
 * @author Mario A. Serrano Leones
 */
public enum ReportOutputType {

    PDF("pdf", "application/pdf"),
    EXCEL("xlsx", "application/vnd.ms-excel"),
    OPENOFFICE("odt", "application/vnd.oasis.opendocument.text"),
    HTML("html", "text/html"),
    CSV("csv", "text/plain"),
    JAVA2D("", ""),
    PLAIN("txt", "text/plain"),
    PRINTER("printer", null);

    private final String extension;
    private final String contentType;

    ReportOutputType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

}
