package config.internal;

import config.TypeConverter;

public class StringConverter implements TypeConverter<String> {
    @Override
    public boolean handles(Class clazz) {
        return String.class.isAssignableFrom(clazz);
    }

    @Override
    public String convert(Object value) {
        return value.toString();
    }
}
