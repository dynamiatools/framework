package tools.dynamia.commons;

public class DataValueRange<T> {
    private T startValue;
    private T endValue;

    public DataValueRange() {
    }

    public DataValueRange(T startValue, T endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public T getStartValue() {
        return startValue;
    }

    public void setStartValue(T startValue) {
        this.startValue = startValue;
    }

    public T getEndValue() {
        return endValue;
    }

    public void setEndValue(T endValue) {
        this.endValue = endValue;
    }


}
