package config;

import config.internal.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultConfiguration implements ConfigurationSpec {
    private final List<TypeConverter> converters = new ArrayList<TypeConverter>()  {{
        add(new StringConverter());
        add(new IntegerConverter());
        add(new LongConverter());
        add(new MapConverter());
    }};
    private final List<ConfigurationSourceSpec> configSources = new ArrayList<>();

    @Override
    public ConfigurationSpec add(ConfigurationSourceSpec configSource) {
        this.configSources.add(configSource);
        return this;
    }

    @Override
    public ConfigurationSpec converter(TypeConverter binder) {
        this.converters.add(binder);
        return this;
    }

    @Override
    public <T> T get(Class<T> configClass) throws BindingException {
        try {
            Constructor<T> constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T configObj = constructor.newInstance();
            bind(getBindingMap(), configObj);
            return configObj;
        } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
            throw new BindingException(e);
        }
    }

    @Override
    public <T> T get(T configObj) throws BindingException {
        bind(getBindingMap(), configObj);
        return configObj;
    }

    private Map<String, Object> getBindingMap() {
        Map<String, Object> bindingMap = new LinkedHashMap<>();
        for (ConfigurationSourceSpec configSource : configSources) {
            mergeMaps(configSource.getBindingMap(), bindingMap);
        }
        return bindingMap;
    }

    @SuppressWarnings("unchecked")
    private static void mergeMaps(Map<String, Object> source, Map<String, Object> target) {
        for (String key : source.keySet()) {
            Object val = source.get(key);
            if (val instanceof Map) {
                Map<String, Object> targetNestedMap = new LinkedHashMap<>();
                if (target.containsKey(key)) {
                    targetNestedMap = (Map<String, Object>) target.get(key);
                } else {
                    target.put(key, targetNestedMap);
                }
                mergeMaps((Map<String, Object>)val, targetNestedMap);
            } else {
                target.put(key, val);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void bind(Map<String, Object> bindingMap, Object bindingObject) throws BindingException {
        try {
            if (bindingObject instanceof Map) {

            }
            for (Field field : bindingObject.getClass().getDeclaredFields()) {
                String name = field.getName();
                if (!bindingMap.containsKey(name)) {
                    continue;
                }
                field.setAccessible(true);
                Class fieldType = field.getType();

                TypeConverter handler = null;
                for (TypeConverter converter : converters) {
                    if (converter.handles(fieldType)) {
                        handler = converter;
                        break;
                    }
                }

                Object value = bindingMap.get(name);
                if ((value instanceof Map) && !Map.class.isAssignableFrom(fieldType)) {
                    Object nextBindingObject = field.get(bindingObject);

                    if (handler != null && nextBindingObject == null) {
                        nextBindingObject = handler.convert(value);
                    } else if (handler == null && nextBindingObject == null) {
                        Constructor constructor = fieldType.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        nextBindingObject = constructor.newInstance();
                    }

                    bind((Map<String, Object>) value, nextBindingObject);
                    value = nextBindingObject;
                } else if ((value instanceof Map) && Map.class.isAssignableFrom(fieldType)) {
                    
                } else {
                    if (handler != null) {
                        value = handler.convert(value);
                    }
                }
                field.set(bindingObject, value);
            }
        } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
            throw new BindingException(e);
        }
    }
}
