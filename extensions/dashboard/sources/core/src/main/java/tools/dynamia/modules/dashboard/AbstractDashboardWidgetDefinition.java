
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

package tools.dynamia.modules.dashboard;

import java.util.Map;

/**
 * Base implementation of the common {@link DashboardWidgetDefinition} properties. All of them can be set from the
 * dashboard descriptor field params. {@link #update(Map)} does nothing by default.
 *
 * @author Mario Serrano Leones
 */
public abstract class AbstractDashboardWidgetDefinition implements DashboardWidgetDefinition {

    private String name;
    private String title;
    private boolean titleVisible;
    private boolean editable;
    private boolean closable;
    private boolean maximizable;
    private boolean asyncSupported;

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the widget name.
     *
     * @param name the widget name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Sets the widget title.
     *
     * @param title the widget title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isTitleVisible() {
        return titleVisible;
    }

    /**
     * Sets whether the title header is shown.
     *
     * @param titleVisible true to show the title
     */
    public void setTitleVisible(boolean titleVisible) {
        this.titleVisible = titleVisible;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets whether the widget is editable.
     *
     * @param editable true if editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isClosable() {
        return closable;
    }

    /**
     * Sets whether the widget is closable.
     *
     * @param closable true if closable
     */
    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    @Override
    public boolean isMaximizable() {
        return maximizable;
    }

    /**
     * Sets whether the widget is maximizable.
     *
     * @param maximizable true if maximizable
     */
    public void setMaximizable(boolean maximizable) {
        this.maximizable = maximizable;
    }

    @Override
    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    /**
     * Sets whether the widget supports async initialization.
     *
     * @param asyncSupported true if async is supported
     */
    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public void update(Map<String, Object> params) {
        //do nothing by default
    }
}
