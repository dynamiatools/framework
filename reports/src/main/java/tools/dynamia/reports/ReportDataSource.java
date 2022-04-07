package tools.dynamia.reports;

import java.util.Collection;

/**
 * Simple wrapper for report datasource, when is used in report params the param value will be replaced with a
 * proper report engine value. For example
 * <pre>
 * <code>
 *
 * params.put("details",new ReportDatasource(collection));
 *
 * //will be replace to something like:
 *
 * params.put("details",new JRBeanCollectionDatasource(collection));
 * </code>
 * </pre>
 */
public class ReportDataSource {

    private Object value;

    public ReportDataSource(Object value) {
        this.value = value;
    }

    public ReportDataSource(Collection value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
