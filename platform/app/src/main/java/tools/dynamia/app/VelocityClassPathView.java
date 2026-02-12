package tools.dynamia.app;

import org.springframework.core.io.ClassPathResource;
import tools.dynamia.web.ClassPathView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class VelocityClassPathView extends ClassPathView {

    /**
     * Constructs a ClassPathView with the specified classpath resource and content type.
     *
     * @param resource    the classpath resource to be rendered
     * @param contentType the content type of the response
     */
    public VelocityClassPathView(ClassPathResource resource, String contentType) {
        super(resource, contentType);
    }

    @Override
    protected void render(BufferedReader reader, PrintWriter writer, Map<String, Object> model) throws IOException {
        new VelocityTemplateEngine().evaluate(reader, writer, model);
    }
}
