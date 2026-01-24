package tools.dynamia.modules.reports.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.domain.jdbc.Row;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDataEntry {

    private String name;
    private Object value;

    private Map<String, Object> values = new HashMap<>();
    @JsonIgnore
    private boolean singleValue;

    public ReportDataEntry() {
    }

    public ReportDataEntry(String name, Object value, boolean singleValue) {
        this.name = name;
        this.value = value;
        this.singleValue = singleValue;
    }

    public static ReportDataEntry build(List<String> names, Row row) {
        ReportDataEntry entry = new ReportDataEntry();
        entry.singleValue = false;
        names.forEach(name -> {
            entry.values.put(name, row.col(name));
        });
        return entry;
    }

    public static ReportDataEntry build(List<String> names, Object bean) {
        ReportDataEntry entry = new ReportDataEntry();
        entry.singleValue = false;
        entry.name = bean.toString();
        entry.value = bean;
        names.forEach(name -> entry.values.put(name, ObjectOperations.invokeBooleanGetMethod(bean, name)));
        return entry;
    }

    public int compareTo(String field, ReportDataEntry other) {
        Object thisValue = values.get(field);
        Object otherValue = other.getValues().get(field);

        Integer result = null;
        if (thisValue != null && otherValue == null) {
            result = 1;
        } else if (thisValue == null && otherValue != null) {
            result = -1;
        } else if (thisValue == null && otherValue == null) {
            result = 0;
        }

        if (result == null && thisValue instanceof Comparable value1 && otherValue instanceof Comparable value2) {
            result = value1.compareTo(value2);
        }

        if (result == null) {
            result = 0;
        }
        return result;
    }


    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public void setSingleValue(boolean singleValue) {
        this.singleValue = singleValue;
    }
}
