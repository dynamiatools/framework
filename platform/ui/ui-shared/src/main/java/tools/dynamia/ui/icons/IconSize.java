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
package tools.dynamia.ui.icons;

/**
 * Enumeration representing different sizes for icons in the UI. This enum defines three standard sizes: SMALL, NORMAL, and LARGE, each associated with a specific directory name that can be used to locate the corresponding icon resources. The getDir() method allows retrieval of the directory name for each size, facilitating the organization and access of icon assets based on their intended display size.
 *
 * @author Mario A. Serrano Leones
 */
public enum IconSize {

    SMALL("16"), NORMAL("24"), LARGE("32");
    private final String dir;

    IconSize(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
}
