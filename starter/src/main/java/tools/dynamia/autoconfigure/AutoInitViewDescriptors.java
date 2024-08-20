package tools.dynamia.autoconfigure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.viewers.ViewDescriptorFactory;

@Component
@Order()
public class AutoInitViewDescriptors implements CommandLineRunner {

    private final ViewDescriptorFactory viewFactory;

    public AutoInitViewDescriptors(ViewDescriptorFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @Override
    public void run(String... args) throws Exception {
        viewFactory.loadViewDescriptors();
    }
}
