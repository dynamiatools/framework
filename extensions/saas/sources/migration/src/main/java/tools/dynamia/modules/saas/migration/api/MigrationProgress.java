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
 * @param processedEntities Total entities processed so far.
 * @param totalEntities     Total entities expected (0 if unknown).
 * @param message           Human-readable description of the current step.
 * @param processedRecords  Total records processed so far (across all entities).
 * @author Mario Serrano Leones
 */
public record MigrationProgress(long processedEntities, long totalEntities, String message, long processedRecords) {

    /**
     * Returns the progress as a percentage (0–100), or -1 if total is unknown.
     */
    public int percentage() {
        if (totalEntities <= 0) return -1;
        return (int) Math.min(100, (processedEntities * 100L) / totalEntities);
    }

    @Override
    public String toString() {
        if (totalEntities > 0) {
            return "[%d%%] %s (%d / %d)".formatted(percentage(), message, processedEntities, totalEntities);
        }
        return "[?] %s (%d processed)".formatted(message, processedEntities);
    }
}

