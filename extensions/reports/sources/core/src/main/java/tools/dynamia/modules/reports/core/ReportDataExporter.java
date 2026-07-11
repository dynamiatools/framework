package tools.dynamia.modules.reports.core;

/**
 * Converts {@link ReportData} into a specific output representation.
 *
 * @param <T> the exported output type (for example, a map, DTO, or serialized structure)
 */
public interface ReportDataExporter<T> {

    /**
     * Exports the provided report data into the target representation.
     *
     * @param reportData the report data to transform
     * @return the exported representation of the report data
     */
    T export(ReportData reportData);
}
