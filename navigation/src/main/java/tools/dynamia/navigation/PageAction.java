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
package tools.dynamia.navigation;

import tools.dynamia.actions.Action;

import java.io.Serializable;

/**
 * Action related to a {@link Page}
 */
public class PageAction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4096406460140799114L;
    private Page page;
    private String id;
    private String name;
    private String description;
    private String image;
    private String actionClass;
    private double position;
    private boolean featured;

    private Action delegate;

    public PageAction() {

    }

    public PageAction(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PageAction(Page page) {
        super();
        this.page = page;
    }

    public PageAction(Page page, String id, String name) {
        super();
        this.id = id;
        this.page = page;
        this.name = name;
    }

    public PageAction(Page page, Action delegate) {
        this.page = page;
        this.delegate = delegate;
        this.id = delegate.getId();
        this.name = delegate.getName();
        this.image = delegate.getImage();
        this.description = delegate.getDescription();
        this.actionClass = delegate.getClass().getName();
        this.position = delegate.getPosition();
    }

    public PageAction(Page page, String id, String name, String description) {
        super();
        this.id = id;
        this.page = page;
        this.name = name;
        this.description = description;
    }

    public PageAction(Page page, String id, String name, String description, String image) {
        super();
        this.id = id;
        this.page = page;
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public void setDelegate(Action delegate) {
        this.delegate = delegate;
    }

    public PageAction image(String image) {
        setImage(image);
        return this;
    }

    public PageAction icon(String icon) {
        setIcon(icon);
        return this;
    }

    public PageAction name(String name) {
        setName(name);
        return this;
    }

    public PageAction id(String id) {
        setId(id);
        return this;
    }

    public PageAction description(String description) {
        setDescription(description);
        return this;
    }

    public PageAction actionClass(String actionClass) {
        setActionClass(actionClass);
        return this;
    }

    public PageAction featured() {
        setFeatured(true);
        return this;
    }

    public PageAction position(double position) {
        setPosition(position);
        return this;
    }


    @Override
    public String toString() {
        return name;
    }

    public String getActionClass() {
        return actionClass;
    }

    public void setActionClass(String actionClass) {
        this.actionClass = actionClass;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public void setIcon(String icon) {
        setImage(icon);
    }

    public String getIcon() {
        return getImage();
    }

    public Action getDelegate() {
        return delegate;
    }

}
