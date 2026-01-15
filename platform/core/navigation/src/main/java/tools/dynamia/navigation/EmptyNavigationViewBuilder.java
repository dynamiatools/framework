package tools.dynamia.navigation;

/***
 * Simple Navigation view builder that do nothing. Return itself has navigation view
 */
public class EmptyNavigationViewBuilder implements NavigationViewBuilder<Object> {
    @Override
    public Object getNavigationView() {
        return this;
    }

    @Override
    public void createModuleView(Module module) {

    }

    @Override
    public void createPageGroupView(PageGroup pageGroup) {

    }

    @Override
    public void createPageView(Page page) {

    }
}
