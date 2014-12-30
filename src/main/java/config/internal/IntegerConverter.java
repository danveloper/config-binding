package config.internal;

import config.TypeConverter;

public class IntegerConverter implements TypeConverter<Integer> {
    @Override
    public boolean handles(Class clazz) {
        return clazz == int.class || Integer.class.isAssignableFrom(clazz);
    }

    @Override
    public Integer convert(Object value) {
        return Integer.valueOf(value.toString());
    }
}
