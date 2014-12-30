package config;

public interface TypeConverter<T> {
    boolean handles(Class clazz);
    T convert(Object value);
}
