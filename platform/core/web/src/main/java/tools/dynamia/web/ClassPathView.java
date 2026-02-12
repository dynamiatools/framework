package tools.dynamia.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.view.AbstractView;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A view that renders content from a classpath resource. It reads the resource line by line and writes it to the HTTP response.
 * Extends this class and overrify renderMergedOutputModel to customize the rendering behavior using for example a template engine or
 * something diferent.
 */
public class ClassPathView extends AbstractView {

    private final ClassPathResource resource;

    /**
     * Constructs a ClassPathView with the specified classpath resource and content type.
     *
     * @param resource    the classpath resource to be rendered
     * @param contentType the content type of the response
     */
    public ClassPathView(ClassPathResource resource, String contentType) {
        this.resource = resource;
        setContentType(contentType);
    }

    /**
     * Renders the view by reading the classpath resource and writing its content to the HTTP response.
     *
     * @param model    the model data
     * @param request  the HTTP request
     * @param response the HTTP response
     * @throws Exception if an error occurs during rendering
     */
    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             PrintWriter writer = response.getWriter()) {
            render(reader, writer, model);
        } catch (IOException e) {
            throw new RuntimeException("Error loading view from classpath: " + resource.getPath(), e);
        }
    }

    protected void render(BufferedReader reader, PrintWriter writer, Map<String, Object> model) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            writer.println(line);
        }
    }

    public ClassPathResource getResource() {
        return resource;
    }
}


