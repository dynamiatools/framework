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

package tools.dynamia.domain.util;

/**
 * Utility class to store a value with a label
 */
public class LabelValue implements Comparable<LabelValue> {
    private String label;
    private Object value;
    private Object auxValue;
    private int order;
    private String classifier;


    public LabelValue() {
    }

    public LabelValue(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public LabelValue(String label, Object value, String classifier) {
        this.label = label;
        this.value = value;
        this.classifier = classifier;
    }

    public LabelValue(String label, Object value, int order) {
        this.label = label;
        this.value = value;
        this.order = order;
    }

    public LabelValue(String label, Object value, int order, String classifier) {
        this.label = label;
        this.value = value;
        this.order = order;
        this.classifier = classifier;
    }

    @Override
    public int compareTo(LabelValue labelValue) {
        return Integer.compare(order, labelValue.order);
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return label;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public Object getAuxValue() {
        return auxValue;
    }

    public void setAuxValue(Object auxValue) {
        this.auxValue = auxValue;
    }
}
