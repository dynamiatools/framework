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
package tools.dynamia.io.impl;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;


/**
 * The Class SpringResource.
 *
 * @author Mario A. Serrano Leones
 */
public class SpringResource implements Resource {

    /**
     * The internal resource.
     */
    private final org.springframework.core.io.Resource internalResource;

    /**
     * Instantiates a new spring resource.
     *
     * @param internalResource the internal resource
     */
    public SpringResource(org.springframework.core.io.Resource internalResource) {
        this.internalResource = internalResource;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return internalResource.getInputStream();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getLastModified()
     */
    @Override
    public long getLastModified() throws IOException {
        return internalResource.lastModified();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#isReadable()
     */
    @Override
    public boolean isReadable() {
        return internalResource.isReadable();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#isOpen()
     */
    @Override
    public boolean isOpen() {
        return internalResource.isOpen();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getURL()
     */
    @Override
    public URL getURL() throws IOException {
        return internalResource.getURL();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getURI()
     */
    @Override
    public URI getURI() throws IOException {
        return internalResource.getURI();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getFilename()
     */
    @Override
    public String getFilename() {
        return internalResource.getFilename();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getFile()
     */
    @Override
    public File getFile() throws IOException {
        return internalResource.getFile();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#exists()
     */
    @Override
    public boolean exists() {
        return internalResource.exists();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getContentLength()
     */
    @Override
    public long getContentLength() throws IOException {
        return internalResource.contentLength();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.io.Resource#getFileExtension()
     */
    @Override
    public String getFileExtension() {
        return StringUtils.getFilenameExtension(getFilename());
    }

    public org.springframework.core.io.Resource getInternalResource() {
        return internalResource;
    }
}
