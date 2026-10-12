package tools.dynamia.demos.movies.providers;

import tools.dynamia.crud.CrudPage;
import tools.dynamia.demos.movies.domain.*;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;

/**
 * Invisible module whose only purpose is to publish read-only REST endpoints under {@code /api/public/**}
 * (open to anonymous users by the security module) for the public movie site. Each {@link CrudPage} gets the
 * automatic endpoints of DynamiaTools; {@link tools.dynamia.demos.movies.config.PublicApiReadOnlyCustomizer}
 * removes every write verb from them. Pages are {@code alwaysAllowed} so anonymous visitors skip the
 * navigation restrictions of the security module.
 */
@Provider
public class PublicApiModuleProvider implements ModuleProvider {

    public static final String MODULE_ID = "public";

    @Override
    public Module getModule() {
        return new Module(MODULE_ID, "Public API")
                .visible(false)
                .position(99)
                .addPage(
                        new CrudPage("movies", "Movies", Movie.class).alwaysAllowed(true),
                        new CrudPage("people", "People", Person.class).alwaysAllowed(true),
                        new CrudPage("genres", "Genres", Genre.class).alwaysAllowed(true),
                        new CrudPage("studios", "Studios", Studio.class).alwaysAllowed(true),
                        new CrudPage("credits", "Credits", Credit.class).alwaysAllowed(true),
                        new CrudPage("reviews", "Reviews", Review.class).alwaysAllowed(true)
                );
    }
}
