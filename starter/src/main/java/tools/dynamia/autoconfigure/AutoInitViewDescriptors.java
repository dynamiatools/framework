package tools.dynamia.autoconfigure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.viewers.ViewDescriptorFactory;

/**
 * Auto-initializes view descriptors at application startup.
 * <p>
 * This Spring component implements {@link CommandLineRunner} to automatically load all view descriptors
 * using the {@link ViewDescriptorFactory} when the application context is initialized. This ensures that
 * all view definitions are available for use throughout the application lifecycle.
 * </p>
 * <p>
 * The {@link Order} annotation can be used to control the execution order of this runner relative to other runners.
 * </p>
 */
@Component
@Order()
public class AutoInitViewDescriptors implements CommandLineRunner {

    /**
     * Factory responsible for loading and managing view descriptors.
     */
    private final ViewDescriptorFactory viewFactory;

    /**
     * Constructs a new {@code AutoInitViewDescriptors} with the specified {@link ViewDescriptorFactory}.
     *
     * @param viewFactory the factory used to load view descriptors; must not be null
     */
    public AutoInitViewDescriptors(ViewDescriptorFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    /**
     * Loads all view descriptors at application startup.
     * <p>
     * This method is invoked by Spring Boot after the application context has been initialized.
     * It delegates to {@link ViewDescriptorFactory#loadViewDescriptors()} to perform the actual loading process.
     * </p>
     *
     * @param args command line arguments passed to the application (not used)
     * @throws Exception if an error occurs during view descriptor loading
     */
    @Override
    public void run(String... args) throws Exception {
        viewFactory.loadViewDescriptors();
    }
}
