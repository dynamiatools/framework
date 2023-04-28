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

import java.io.Serializable;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class Icon implements Serializable {

    public final static Icon NONE = new Icon(null, null, null, null, IconType.IMAGE);
    private final String name;
    private final String internalName;
    private final String directory;
    private final String extension;
    private IconType type = IconType.IMAGE;

    /**
     *
     * @param name
     * @param directory
     * @param extension
     */
    public Icon(String name, String directory, String extension) {
        this(name, name, directory, extension, IconType.IMAGE);
    }

    /**
     *
     * @param name
     * @param internalName
     * @param type
     */
    public Icon(String name, String internalName, IconType type) {
        this(name, internalName, null, null, type);
    }

    /**
     *
     * @param name
     * @param internalName
     * @param directory
     * @param extension
     * @param type
     */
    public Icon(String name, String internalName, String directory, String extension, IconType type) {
        this.name = name;
        this.internalName = internalName;
        this.directory = directory;
        this.extension = extension;
        this.type = type;
    }

    public String getExtension() {
        return extension;
    }

    public String getRealPath() {
        return getRealPath(IconSize.NORMAL);
    }

    public String getRealPath(IconSize size) {
        return getRealPath(null, size);
    }

    public String getRealPath(Object targetComponent) {
        return getRealPath(targetComponent, IconSize.NORMAL);
    }

    public String getRealPath(Object targetComponent, IconSize size) {
        if (name == null) {
            return null;
        }

        String realPath = directory + "/" + size.getDir() + "/" + name + "." + extension;
        if (type == IconType.FONT) {
            realPath = internalName;
        }

        return realPath;
    }

    public String getName() {
        return name;
    }

    public IconType getType() {
        return type;
    }

}
