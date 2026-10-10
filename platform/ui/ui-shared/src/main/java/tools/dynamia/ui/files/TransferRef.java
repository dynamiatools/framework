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
 * Reference to a file in a {@link TransferStore}: what travels in JSON and in resume tokens instead of the content.
 *
 * @param ref         identifier in the store; unguessable
 * @param name        file name
 * @param contentType MIME type, may be null
 * @param size        size in bytes
 */
public record TransferRef(String ref, String name, String contentType, long size) {
}
