/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;


/**
 * The Interface Resource.
 *
 * @author Mario A. Serrano Leones
 */
public interface Resource {

    /**
     * Gets the content length.
     *
     * @return the content length
     * @throws IOException Signals that an I/O exception has occurred.
     */
    long getContentLength() throws IOException;

    /**
     * Exists.
     *
     * @return true, if successful
     */
    boolean exists();

    /**
     * Gets the file.
     *
     * @return the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    File getFile() throws IOException;

    /**
     * Gets the filename.
     *
     * @return the filename
     */
    String getFilename();

    /**
     * Gets the file extension.
     *
     * @return the file extension
     */
    String getFileExtension();

    /**
     * Gets the input stream.
     *
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Gets the url.
     *
     * @return the url
     * @throws IOException Signals that an I/O exception has occurred.
     */
    URL getURL() throws IOException;

    /**
     * Gets the uri.
     *
     * @return the uri
     * @throws IOException Signals that an I/O exception has occurred.
     */
    URI getURI() throws IOException;

    /**
     * Checks if is open.
     *
     * @return true, if is open
     */
    boolean isOpen();

    /**
     * Checks if is readable.
     *
     * @return true, if is readable
     */
    boolean isReadable();

    /**
     * Gets the last modified.
     *
     * @return the last modified
     * @throws IOException Signals that an I/O exception has occurred.
     */
    long getLastModified() throws IOException;
}
