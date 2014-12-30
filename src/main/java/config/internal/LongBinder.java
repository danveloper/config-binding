package config.internal;

public class LongBinder extends AbstractTypeBinder<Long> {
    @Override
    public Class<Long> getType() {
        return Long.class;
    }
}
