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

package tools.dynamia.modules.saas.api;

import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.integration.Containers;

import java.util.Date;

/**
 * Interface for managing account-specific temporary storage in a multi-tenant SaaS environment.
 * <p>
 * This interface provides a key-value storage mechanism that is scoped to the current account,
 * useful for caching data, storing temporary state, or maintaining session-like information
 * at the account level. The storage is typically backed by in-memory caching or similar mechanisms.
 * <p>
 * All stored entries include metadata such as timestamps and optional messages,
 * making it suitable for audit trails or debugging purposes.
 * <p>
 * Example usage:
 * <pre>{@code
 * AccountLocalStorage storage = AccountLocalStorage.load();
 * storage.add("last-sync-time", new Date());
 * Date lastSync = (Date) storage.get("last-sync-time");
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountLocalStorage {

    /**
     * Loads the current account's local storage instance.
     * <p>
     * This static factory method retrieves the active implementation of AccountLocalStorage
     * from the dependency injection container, automatically scoped to the current account context.
     *
     * @return the AccountLocalStorage instance for the current account
     */
    static AccountLocalStorage load() {
        return Containers.get().findObject(AccountLocalStorage.class);
    }

    /**
     * Retrieves a value from storage by its key.
     *
     * @param key the storage key
     * @return the stored value, or null if not found
     */
    Object get(String key);

    /**
     * Retrieves a complete entry from storage, including metadata.
     *
     * @param key the storage key
     * @return the Entry object containing value and metadata, or null if not found
     */
    Entry getEntry(String key);

    /**
     * Stores a value with the specified key.
     * <p>
     * If a value already exists for this key, it will be replaced.
     *
     * @param key the storage key
     * @param value the value to store
     */
    void add(String key, Object value);

    /**
     * Stores a complete entry with metadata.
     *
     * @param entry the entry to store, containing key, value, and optional message
     */
    void addEntry(Entry entry);

    /**
     * Removes a value from storage.
     *
     * @param key the storage key to remove
     */
    void remove(String key);

    /**
     * Clears all entries from the current account's storage.
     */
    void clear();

    /**
     * Represents a storage entry with metadata including timestamp and optional message.
     * <p>
     * Each entry captures not only the key-value pair but also when it was created
     * and an optional descriptive message, useful for audit trails or debugging.
     */
    class Entry {
        private final String key;
        private final Object value;
        private final long timestamp;
        private final String message;
        private final Date date;

        /**
         * Creates a new entry with the specified key and value.
         *
         * @param key the storage key
         * @param value the value to store
         */
        public Entry(String key, Object value) {
            this(key, value, null);
        }

        /**
         * Creates a new entry with the specified key, value, and descriptive message.
         *
         * @param key the storage key
         * @param value the value to store
         * @param message an optional descriptive message for this entry
         */
        public Entry(String key, Object value, String message) {
            this.key = key;
            this.value = value;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
            this.date = DateTimeUtils.createDate(timestamp);
        }

        /**
         * Returns the storage key for this entry.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the stored value.
         *
         * @return the value
         */
        public Object getValue() {
            return value;
        }

        /**
         * Returns the timestamp (milliseconds since epoch) when this entry was created.
         *
         * @return the creation timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Returns the optional descriptive message associated with this entry.
         *
         * @return the message, or null if none was provided
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the creation date of this entry.
         *
         * @return the creation date
         */
        public Date getDate() {
            return date;
        }
    }
}
