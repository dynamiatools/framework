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
package tools.dynamia.io;

/**
 * Exception thrown to indicate an error occurred during file operations.
 * <p>
 * This exception is a runtime exception and can be used to wrap IO errors or other file-related issues.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 *     try {
 *         // some file operation
 *     } catch (IOException e) {
 *         throw new FileException("Error reading file", e);
 *     }
 * </pre>
 *
 * @author Dynamia Soluciones IT S.A.S
 * @since 1.0
 */
public class FileException extends RuntimeException {
    /**
     * Serial version UID for serialization.
     */
    private static final long serialVersionUID = -8737178149824933689L;

    /**
     * Constructs a new FileException with {@code null} as its detail message.
     */
    public FileException() {
        super();
    }

    /**
     * Constructs a new FileException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public FileException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new FileException with the specified detail message.
     *
     * @param message the detail message
     */
    public FileException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileException with the specified cause.
     *
     * @param cause the cause
     */
    public FileException(Throwable cause) {
        super(cause);
    }
}
