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
package tools.dynamia.modules.saas.migration.pipeline;

/**
 * Constants shared between the export and import pipelines.
 *
 * @author Mario Serrano Leones
 */
public final class ExportConstants {

    private ExportConstants() {
    }

    /** Current format version written to every export file. */
    public static final String FORMAT_VERSION = "1";

    /**
     * Suffix appended to field names when serializing {@code @ManyToOne} /
     * {@code @OneToOne} references. For example, a {@code category} field is
     * exported as {@code category_ref_id} containing only the referenced entity's
     * primary key value.
     */
    public static final String REF_ID_SUFFIX = "_ref_id";

    /** Top-level JSON field containing the serialized AccountDTO. */
    public static final String FIELD_ACCOUNT = "account";

    /** Top-level JSON field containing the entity data map. */
    public static final String FIELD_ENTITIES = "entities";

    /** Top-level JSON field for the format version string. */
    public static final String FIELD_VERSION = "version";

    /** Top-level JSON field for the ISO-8601 export timestamp. */
    public static final String FIELD_EXPORTED_AT = "exportedAt";

    /** Top-level JSON field for the source account ID. */
    public static final String FIELD_SOURCE_ACCOUNT_ID = "sourceAccountId";

    /** Top-level JSON field for the identity strategy name. */
    public static final String FIELD_IDENTITY_STRATEGY = "identityStrategy";
}

