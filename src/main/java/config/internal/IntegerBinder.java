package config.internal;

public class IntegerBinder extends AbstractTypeBinder<Integer> {
    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }
}
