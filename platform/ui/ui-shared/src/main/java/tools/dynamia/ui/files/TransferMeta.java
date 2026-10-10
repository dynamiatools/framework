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
package tools.dynamia.ui.files;

/**
 * What a {@link TransferStore} needs to know about a file it holds.
 *
 * @param name        file name
 * @param contentType MIME type, may be null
 * @param direction   whether the user is uploading it or will download it
 * @param subject     user the file belongs to
 * @param tenant      tenant the file belongs to, may be null
 */
public record TransferMeta(String name, String contentType, Direction direction, String subject, String tenant) {

    /** Which way the file goes. */
    public enum Direction {
        /** From the user to the action. */
        UPLOAD,
        /** From the action to the user. */
        DOWNLOAD
    }

    /**
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param direction   direction
     * @param owner       who it belongs to
     * @return the metadata
     */
    public static TransferMeta of(String name, String contentType, Direction direction, FlowPrincipal owner) {
        return new TransferMeta(name, contentType, direction, owner.subject(), owner.tenant());
    }

    /**
     * @param owner a principal
     * @return whether the file belongs to it
     */
    public boolean isOwnedBy(FlowPrincipal owner) {
        return owner != null && java.util.Objects.equals(subject, owner.subject())
                && java.util.Objects.equals(tenant, owner.tenant());
    }
}
