/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.modules.saas.migration.api;

/**
 * Carries progress information during a tenant migration operation.
 *
 * @param processedRecords Total records processed so far.
 * @param totalRecords     Total records expected (0 if unknown).
 * @param message          Human-readable description of the current step.
 * @author Mario Serrano Leones
 */
public record MigrationProgress(long processedRecords, long totalRecords, String message) {

    /** Returns the progress as a percentage (0–100), or -1 if total is unknown. */
    public int percentage() {
        if (totalRecords <= 0) return -1;
        return (int) Math.min(100, (processedRecords * 100L) / totalRecords);
    }

    @Override
    public String toString() {
        if (totalRecords > 0) {
            return "[%d%%] %s (%d / %d)".formatted(percentage(), message, processedRecords, totalRecords);
        }
        return "[?] %s (%d processed)".formatted(message, processedRecords);
    }
}

