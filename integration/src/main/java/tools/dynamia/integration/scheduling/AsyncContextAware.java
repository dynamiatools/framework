package tools.dynamia.integration.scheduling;

public interface AsyncContextAware<T> {

    T getContextObject();
}
