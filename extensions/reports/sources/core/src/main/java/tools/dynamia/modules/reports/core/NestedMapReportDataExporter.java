package tools.dynamia.modules.reports.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NestedMapReportDataExporter implements ReportDataExporter<Map<String, Object>> {

    @Override
    public Map<String, Object> export(ReportData reportData) {

        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("data", data);

        reportData.getEntries().forEach(e -> {
            data.add(groupValues(e.getValues()));
        });

        if (reportData.isTruncated()) {
            map.put("truncated", true);
        }

        return map;

    }
    public static Map<String, Object> groupValues(Map<String, Object> inputMap) {
        Map<String, Object> resultMap = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            addToNestedMap(resultMap, entry.getKey(), entry.getValue());
        }

        return resultMap;
    }

    private static void addToNestedMap(Map<String, Object> map, String key, Object value) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = map;

        for (int i = 0; i < keys.length - 1; i++) {
            String currentKey = keys[i];
            if (!currentMap.containsKey(currentKey)) {
                currentMap.put(currentKey, new LinkedHashMap<>());
            }
            Object nestedValue = currentMap.get(currentKey);
            if (!(nestedValue instanceof Map)) {
                // Si el valor no es un mapa, reemplácelo por un nuevo mapa
                nestedValue = new LinkedHashMap<>();
                currentMap.put(currentKey, nestedValue);
            }
            currentMap = (Map<String, Object>) nestedValue;
        }

        currentMap.put(keys[keys.length - 1], value);
    }
}
