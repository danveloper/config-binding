package config;

import config.internal.BindingException;

public interface ConfigurationSpec {

    ConfigurationSpec add(ConfigurationSourceSpec configSource);

    ConfigurationSpec binder(TypeBinder binder);

    <T> T get(Class<T> configClass) throws BindingException;

    <T> T get(T configObj) throws BindingException;
}
