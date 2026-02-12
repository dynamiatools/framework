package tools.dynamia.navigation;

/***
 * Simple and basic implementation
 */
public class DefaultNavigationBuilder extends NavigationBuilder {
    @Override
    protected void showNavigation(Object navigationView) {

    }

    @Override
    protected NavigationViewBuilder defaultViewVuilder() {
        return new EmptyNavigationViewBuilder();
    }
}
