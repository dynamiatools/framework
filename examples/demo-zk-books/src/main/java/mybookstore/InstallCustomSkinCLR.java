package mybookstore;

import org.springframework.boot.CommandLineRunner;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.templates.ApplicationTemplateSkin;
import tools.dynamia.themes.dynamical.DynamicalTemplate;

@Provider
public class InstallCustomSkinCLR implements CommandLineRunner {


    private final DynamicalTemplate template;

    public InstallCustomSkinCLR(DynamicalTemplate template) {
        this.template = template;
    }

    @Override
    public void run(String... args) throws Exception {
        var custom = new ApplicationTemplateSkin("books", "Books Style", "skin-green-min.css", "Custom skin");
        custom.setCustomLayout(true);
        custom.setLayoutView("books/template");
        template.installSkin(custom);
    }
}
