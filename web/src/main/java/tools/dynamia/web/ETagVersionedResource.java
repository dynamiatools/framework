package tools.dynamia.web;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.resource.HttpResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

public class ETagVersionedResource extends AbstractResource implements HttpResource {

    private final Resource original;

    private final String version;

    public ETagVersionedResource(Resource original, String version) {
        this.original = original;
        this.version = version;
    }

    @Override
    public boolean exists() {
        return this.original.exists();
    }

    @Override
    public boolean isReadable() {
        return this.original.isReadable();
    }

    @Override
    public boolean isOpen() {
        return this.original.isOpen();
    }

    @Override
    public boolean isFile() {
        return this.original.isFile();
    }

    @Override
    public URL getURL() throws IOException {
        return this.original.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return this.original.getURI();
    }

    @Override
    public File getFile() throws IOException {
        return this.original.getFile();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.original.getInputStream();
    }

    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        return this.original.readableChannel();
    }

    @Override
    public byte[] getContentAsByteArray() throws IOException {
        return this.original.getContentAsByteArray();
    }

    @Override
    public String getContentAsString(Charset charset) throws IOException {
        return this.original.getContentAsString(charset);
    }

    @Override
    public long contentLength() throws IOException {
        return this.original.contentLength();
    }

    @Override
    public long lastModified() throws IOException {
        return this.original.lastModified();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return this.original.createRelative(relativePath);
    }

    @Override
    @Nullable
    public String getFilename() {
        return this.original.getFilename();
    }

    @Override
    public String getDescription() {
        return this.original.getDescription();
    }

    @Override
    public HttpHeaders getResponseHeaders() {
        HttpHeaders headers = (this.original instanceof HttpResource httpResource ?
                httpResource.getResponseHeaders() : new HttpHeaders());
        headers.setETag("W/\"" + this.version + "\"");
        return headers;
    }
}