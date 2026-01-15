package tools.dynamia.domain;


/**
 * Helper class to disable {@link OpenPersistenceInViewProvider}. Create a {@link org.springframework.context.annotation.Primary} bean instance
 * of this class to override default provider.
 */
public class NoOpenPersistenceInViewProvider implements OpenPersistenceInViewProvider {
    @Override
    public boolean beforeView() {
        return false;
    }

    @Override
    public void afterView(boolean participate) {
//do nothing
    }

    @Override
    public boolean isDisabled() {
        return true;
    }
}
