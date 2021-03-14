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
package tools.dynamia.viewers;


/**
 * The Class ViewDescriptorTarget.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorTarget {

    /**
     * The Constant ALL.
     */
    public static final ViewDescriptorTarget ALL = new ViewDescriptorTarget();

    /**
     * The bean class.
     */
    private Class beanClass;

    /**
     * The type.
     */
    private String type;

    /**
     * The id.
     */
    private String id;

    /**
     * Instantiates a new view descriptor target.
     */
    public ViewDescriptorTarget() {
    }

    /**
     * Instantiates a new view descriptor target.
     *
     * @param beanClass the bean class
     * @param type the type
     * @param id the id
     */
    public ViewDescriptorTarget(Class beanClass, String type, String id) {
        this.beanClass = beanClass;
        this.type = type;
        this.id = id;
    }

    /**
     * Instantiates a new view descriptor target.
     *
     * @param type the type
     * @param id the id
     */
    public ViewDescriptorTarget(String type, String id) {
        this.type = type;
        this.id = id;
    }

    /**
     * Instantiates a new view descriptor target.
     *
     * @param id the id
     */
    public ViewDescriptorTarget(String id) {
        this.id = id;
    }

    /**
     * Gets the bean class.
     *
     * @return the bean class
     */
    public Class getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the bean class.
     *
     * @param beanClass the new bean class
     */
    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(String id) {
        this.id = id;
    }

}
