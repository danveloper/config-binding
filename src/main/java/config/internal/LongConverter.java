package config.internal;

import config.TypeConverter;

public class LongConverter implements TypeConverter<Long> {
    @Override
    public boolean handles(Class clazz) {
        return clazz == long.class || Long.class.isAssignableFrom(clazz);
    }

    @Override
    public Long convert(Object value) {
        return Long.valueOf(value.toString());
    }
}
