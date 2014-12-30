package config;

import config.internal.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultConfiguration implements ConfigurationSpec {
    private final List<TypeBinder> binders = new ArrayList<TypeBinder>()  {{
        add(new StringBinder());
        add(new IntegerBinder());
        add(new LongBinder());
        add(new MapBinder());
    }};
    private final List<ConfigurationSourceSpec> configSources = new ArrayList<>();

    @Override
    public ConfigurationSpec add(ConfigurationSourceSpec configSource) {
        this.configSources.add(configSource);
        return this;
    }

    @Override
    public ConfigurationSpec binder(TypeBinder binder) {
        this.binders.add(binder);
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

    private static void mergeMaps(Map<String, Object> source, Map<String, Object> target) {
        for (String key : source.keySet()) {
            Object val = source.get(key);
            if ((!target.containsKey(key)) && val instanceof Map) {
                Map<String, Object> targetNestedMap = (Map<String, Object>)target.get(key);
                mergeMaps((Map<String, Object>)val, targetNestedMap);
            } else {
                target.put(key, val);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void bind(Map<String, Object> bindingMap, Object bindingObject) throws BindingException {
        try {
            for (Field field : bindingObject.getClass().getDeclaredFields()) {
                String name = field.getName();
                if (!bindingMap.containsKey(name)) {
                    continue;
                }
                Object value = bindingMap.get(name);
                Class fieldType = field.getType();
                TypeBinder handler = null;
                for (TypeBinder binder : binders) {
                    if (binder.getType().isAssignableFrom(fieldType)) {
                        handler = binder;
                        break;
                    }
                }
                if (handler != null) {
                    handler.bind(bindingObject, field, fieldType.cast(value));
                } else if (value instanceof Map) {
                    Object nextBindingObject = field.get(bindingObject);

                    if (nextBindingObject == null) {
                        Constructor constructor = fieldType.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        nextBindingObject = constructor.newInstance();
                    }

                    bind((Map<String, Object>) value, nextBindingObject);
                    field.setAccessible(true);
                    field.set(bindingObject, nextBindingObject);
                }
            }
        } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
            throw new BindingException(e);
        }
    }
}
