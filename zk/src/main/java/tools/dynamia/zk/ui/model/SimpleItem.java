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

package tools.dynamia.zk.ui.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimpleItem implements Serializable {

    private String label;
    private Object value;

    public SimpleItem() {

    }

    public SimpleItem(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public SimpleItem(Object value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return label;
    }

    public static List<SimpleItem> parse(Map<String, Object> map) {
        List<SimpleItem> items = new ArrayList<>();
        map.keySet().stream().sorted().forEach(k -> {
            Object v = map.get(k);
            SimpleItem item = new SimpleItem();
            if (v instanceof Map) {
                item.setLabel((String) ((Map) v).get("label"));
                item.setValue(((Map) v).get("value"));
            } else {
                item.setLabel(k);
                item.setValue(v);
            }
            items.add(item);
        });


        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleItem that = (SimpleItem) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
