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

package tools.dynamia.zk.ui;

import org.zkoss.zul.Label;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.util.Objects;

/**
 * Label commponent for enums
 */
public class EnumLabel extends Label  implements LoadableOnly{

    static {
        ComponentAliasIndex.getInstance().add("enumlabel", EnumLabel.class);
        BindingComponentIndex.getInstance().put("enum", EnumLabel.class);
    }

    private String sclassPrefix = "";
    private String defaultSclass = "";

    private Enum enumValue;


    public Enum getEnum() {
        return enumValue;
    }

    public void setEnum(Enum enumValue) {
        if (!Objects.equals(this.enumValue, enumValue)) {
            this.enumValue = enumValue;
            setValue(BeanUtils.getInstanceName(enumValue));
        }

    }

    @Override
    public void setValue(String value) {
        if (!Objects.equals(getValue(), value)) {
            super.setValue(value);
            renderStyles();
        }
    }

    public String getSclassPrefix() {
        return sclassPrefix;
    }

    public void setSclassPrefix(String sclassPrefix) {
        this.sclassPrefix = sclassPrefix;
        renderStyles();
    }

    public String getDefaultSclass() {
        return defaultSclass;
    }

    public void setDefaultSclass(String defaultSclass) {
        this.defaultSclass = defaultSclass;
        renderStyles();
    }

    private void renderStyles() {
        if (enumValue != null) {
            setSclass(defaultSclass + "  " + sclassPrefix + enumValue);
        } else {
            setSclass(defaultSclass + "  " + sclassPrefix + getValue());
        }
    }
}
