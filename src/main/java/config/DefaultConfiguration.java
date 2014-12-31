package config;

import config.internal.*;

import java.lang.reflect.*;
import java.util.*;

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
                    value = bindMap((Map) value, getParameterType(field, 1));
                } else if ((value instanceof Map) && !Map.class.isAssignableFrom(fieldType)) {
                    Object nextBindingObject = field.get(bindingObject);

                    if (nextBindingObject == null) {
                        nextBindingObject = bindObject((Map)value, fieldType);
                    } else {
                        bind((Map<String, Object>) value, nextBindingObject);
                    }
                    value = nextBindingObject;
                } else if ((value instanceof Collection) && Collection.class.isAssignableFrom(fieldType)) {
                    value = bindList((List) value, fieldType, getParameterType(field, 0));
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

    private static <T> Class<T> getParameterType(Field field, int idx) {
        ParameterizedType pt = (ParameterizedType)field.getGenericType();
        Type[] parameterizedTypes = pt.getActualTypeArguments();
        Type mapValType = parameterizedTypes[idx];
        return (Class<T>)mapValType;
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
        TypeConverter handler = getConverter(valClass);

        LinkedHashMap<String, T> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : bindingMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (handler != null) {
                value = handler.convert(value);
            }

            if (value instanceof Map) {
                value = bindObject((Map) value, valClass);
            }
            result.put(key, (T) value);
        }
        return result;
    }

    private <T> Collection<T> bindList(List bindingList, Class fieldType, Class<T> valClass) throws BindingException {
        TypeConverter handler = getConverter(valClass);

        Collection<T> result = new ArrayList<>();
        if (Set.class.isAssignableFrom(fieldType)) {
            result = new LinkedHashSet<>();
        }
        for (Object obj : bindingList) {
            if (handler != null) {
                obj = handler.convert(obj);
            }
            if (valClass.isAssignableFrom(obj.getClass())) {
                result.add((T)obj);
            } else if (obj instanceof Map) {
                result.add((T)bindObject((Map)obj, valClass));
            }
        }
        return result;
    }
}
