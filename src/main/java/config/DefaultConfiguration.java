package config;

import config.internal.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
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

    private <T> TypeConverter<T> getConverter(Class<T> type) {
        TypeConverter<T> handler = null;
        for (TypeConverter converter : converters) {
            if (converter.handles(type)) {
                handler = converter;
                break;
            }
        }
        return handler;
    }

    private void bind(Map<String, Object> bindingMap, Object bindingObject) throws BindingException {
        try {
            for (Field field : bindingObject.getClass().getDeclaredFields()) {
                String name = field.getName();
                if (!bindingMap.containsKey(name)) {
                    continue;
                }
                field.setAccessible(true);
                Class fieldType = field.getType();

                Object value = bindingMap.get(name);
                if ((value instanceof Map) && Map.class.isAssignableFrom(fieldType)) {
                    ParameterizedType pt = (ParameterizedType)field.getGenericType();
                    Type[] parameterizedTypes = pt.getActualTypeArguments();
                    Type mapValType = parameterizedTypes[1];
                    value = bindMap((Map) value, (Class) mapValType);
                } else if ((value instanceof Map) && !Map.class.isAssignableFrom(fieldType)) {
                    Object nextBindingObject = field.get(bindingObject);

                    if (nextBindingObject == null) {
                        nextBindingObject = bindObject((Map)value, fieldType);
                    } else {
                        bind((Map<String, Object>) value, nextBindingObject);
                    }
                    value = nextBindingObject;
                }

                TypeConverter handler = getConverter(fieldType);
                if (handler != null) {
                    value = handler.convert(value);
                }

                field.set(bindingObject, value);
            }
        } catch (IllegalAccessException e) {
            throw new BindingException(e);
        }
    }

    private <T> T bindObject(Map<String, Object> bindingMap, Class<T> objectClass) throws BindingException {
        try {
            Constructor<T> constructor = objectClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T obj = constructor.newInstance();
            bind(bindingMap, obj);
            return obj;
        } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
            throw new BindingException(e);
        }
    }

    private <T> Map<String, T> bindMap(Map<String, Object> bindingMap, Class<T> valClass) throws BindingException {
        LinkedHashMap<String, T> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : bindingMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            TypeConverter handler = getConverter(valClass);

            if (handler != null) {
                value = handler.convert(value);
            } else if (value instanceof Map) {
                value = bindObject((Map)value, valClass);
            }
            result.put(key, (T) value);
        }
        return result;
    }
}
