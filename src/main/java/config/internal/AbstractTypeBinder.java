package config.internal;

import config.TypeBinder;

import java.lang.reflect.Field;

abstract class AbstractTypeBinder<T> implements TypeBinder<T> {
    public abstract Class<T> getType();

    public Class<? extends T> getImplType() {
        return getType();
    }

    public void bind(Object obj, Field field, T value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(obj, value);
    }
}
