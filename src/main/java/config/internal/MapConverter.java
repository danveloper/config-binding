package config.internal;

import config.TypeConverter;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapConverter implements TypeConverter<Map> {
    @Override
    public boolean handles(Class clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    public Map convert(Object value) {
        return new LinkedHashMap<>();
    }
}
