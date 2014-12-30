package config.internal;

public class StringBinder extends AbstractTypeBinder<String> {
    @Override
    public Class<String> getType() {
        return String.class;
    }
}
