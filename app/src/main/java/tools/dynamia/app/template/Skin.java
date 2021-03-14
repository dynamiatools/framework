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
package tools.dynamia.app.template;

import java.io.Serializable;

public class Skin implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4543799834003076100L;
    private String id;
    private String name;
    private String filename;
    private String description;
    private String baseBackgroundColor;
    private String baseColor;

    public Skin(String id, String name, String filename, String description) {
        super();
        this.id = id;
        this.name = name;
        this.filename = filename;
        this.description = description;
    }

    public String getBaseBackgroundColor() {
        return baseBackgroundColor;
    }

    public void setBaseBackgroundColor(String baseBackgroundColor) {
        this.baseBackgroundColor = baseBackgroundColor;
    }

    public String getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(String baseColor) {
        this.baseColor = baseColor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return id;
    }

}
