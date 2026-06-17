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
package tools.dynamia.modules.saas.migration.domain;

/**
 * Lifecycle status of a {@link AccountMigrationJob}.
 *
 * @author Mario Serrano Leones
 */
public enum AccountJobStatus {

    /**
     * Job has been created but not started yet.
     */
    PENDING,

    /**
     * Job is currently executing in a background virtual thread.
     */
    RUNNING,

    /**
     * Job finished successfully. Result file is available for download.
     */
    COMPLETED,

    /**
     * Job failed with an error. See {@link AccountMigrationJob#getErrorMessage()}.
     */
    FAILED,

    /**
     * Job was cancelled by the user before it completed.
     */
    CANCELLED,

    DELETED
}

