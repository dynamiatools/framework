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
package tools.dynamia.viewers.util;

import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.ViewDescriptorReader;
import tools.dynamia.viewers.ViewDescriptorReaderCustomizer;

import java.util.ArrayList;
import java.util.List;


/**
 * The Class ViewDescriptorReaderUtils.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorReaderUtils {

    /**
     * Checks if is file type supported.
     *
     * @param fileExtension the file extension
     * @return true, if is file type supported
     */
    public static boolean isFileTypeSupported(String fileExtension) {
        ViewDescriptorReader vdr = getReaderFor(fileExtension);
        return vdr != null;
    }

    /**
     * Checks if is supported.
     *
     * @param fileExtension the file extension
     * @param reader        the reader
     * @return true, if is supported
     */
    public static boolean isSupported(String fileExtension, ViewDescriptorReader reader) {
        if (reader != null && fileExtension != null && reader.getSupportedFileExtensions() != null) {
            for (String ext : reader.getSupportedFileExtensions()) {
                if (ext != null && ext.equalsIgnoreCase(fileExtension)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the reader for.
     *
     * @param fileExtension the file extension
     * @return the reader for
     */
    public static ViewDescriptorReader getReaderFor(String fileExtension) {
        for (ViewDescriptorReader reader : Containers.get().findObjects(ViewDescriptorReader.class)) {
            if (isSupported(fileExtension, reader)) {
                return reader;
            }
        }
        return null;
    }

    /**
     * Gets the customizers.
     *
     * @param reader the reader
     * @return the customizers
     */
    public static List<ViewDescriptorReaderCustomizer> getCustomizers(ViewDescriptorReader reader) {
        List<ViewDescriptorReaderCustomizer> allowedCustomizers = new ArrayList<>();
        for (ViewDescriptorReaderCustomizer vdrc : Containers.get().findObjects(ViewDescriptorReaderCustomizer.class)) {
            if (reader.getClass().equals(vdrc.getTargetReader())) {
                allowedCustomizers.add(vdrc);
            }
        }
        return allowedCustomizers;
    }

    private ViewDescriptorReaderUtils() {
    }

    public static Object parseValue(String value) {
        Object parsed = value;

        if ("true".equalsIgnoreCase(value)) {
            parsed = Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(value)) {
            parsed = Boolean.FALSE;
        }


        return parsed;
    }
}
