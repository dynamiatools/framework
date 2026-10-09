package tools.dynamia.demos.movies.providers;

import tools.dynamia.crud.CrudPage;
import tools.dynamia.demos.movies.domain.*;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;

/**
 * Backoffice navigation: the catalog CRUD pages the Vue theme renders.
 */
@Provider
public class MoviesModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        return new Module("catalog", "Catalog")
                .icon("film")
                .position(1)
                .addPage(
                        new CrudPage("movies", "Movies", Movie.class).icon("film"),
                        new CrudPage("people", "People", Person.class).icon("users"),
                        new CrudPage("genres", "Genres", Genre.class).icon("tags"),
                        new CrudPage("studios", "Studios", Studio.class).icon("building"),
                        new CrudPage("credits", "Credits", Credit.class).icon("users"),
                        new CrudPage("reviews", "Reviews", Review.class).icon("star")
                );
    }
}
