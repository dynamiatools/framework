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
package tools.dynamia.domain;

import java.util.Date;

public interface Auditable {

    /**
     * Gets the creation date.
     *
     * @return the creation date
     */
    Date getCreationDate();

    /**
     * Gets the creation time.
     *
     * @return the creation time
     */
    Date getCreationTime();

    /**
     * Sets the creation date.
     *
     * @param creationDate the new creation date
     */
    void setCreationDate(Date creationDate);

    /**
     * Sets the creation time.
     *
     * @param creationTime the new creation time
     */
    void setCreationTime(Date creationTime);

    /**
     * Gets the creator.
     *
     * @return the creator
     */
    String getCreator();

    /**
     * Sets the creator.
     *
     * @param creator the new creator
     */
    void setCreator(String creator);

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    Date getLastUpdate();

    /**
     * Sets the last update.
     *
     * @param lastUpdate the new last update
     */
    void setLastUpdate(Date lastUpdate);

    /**
     * Gets the last updater.
     *
     * @return the last updater
     */
    String getLastUpdater();

    /**
     * Sets the last updater.
     *
     * @param lastUpdater the new last updater
     */
    void setLastUpdater(String lastUpdater);

}
