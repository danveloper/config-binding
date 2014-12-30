package config;

import java.lang.reflect.Field;

public interface TypeBinder<T> {

    Class<T> getType();

    Class<? extends T> getImplType();

    void bind(Object obj, Field field, T value) throws IllegalAccessException;
}
