package config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    static final List<BindingSpec> binders = new ArrayList<BindingSpec>() {{
        add(new StringBinder());
        add(new IntegerBinder());
        add(new LongBinder());
        add(new MapBinder());
    }};

    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException,
            NoSuchMethodException, InvocationTargetException {
        Properties props = new Properties();
        props.load(Main.class.getResourceAsStream("/app.properties"));
        Class configClass = MyConfig.class;

        Map<String, Object> bindingMap = new HashMap<>();
        for (Object objKey : props.keySet()) {
            if (!(objKey instanceof String)) {
                continue;
            }

            String key = (String) objKey;
            buildMappings(key, bindingMap, props.getProperty(key));
        }

        Constructor constructor = configClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object configObj = constructor.newInstance();
        bind(bindingMap, configObj);
        System.out.println(configObj);
    }

    private static void bind(Map<String, Object> bindingMap, Object bindingObject) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        for (Field field : bindingObject.getClass().getDeclaredFields()) {
            String name = field.getName();
            if (!bindingMap.containsKey(name)) {
                continue;
            }
            Object value = bindingMap.get(name);
            Class fieldType = field.getType();
            BindingSpec handler = null;
            for (BindingSpec binder : binders) {
                if (binder.getType().isAssignableFrom(fieldType)) {
                    handler = binder;
                    break;
                }
            }
            if (handler != null) {
                handler.bind(bindingObject, field, fieldType.cast(value));
            } else if (value instanceof Map) {
                Constructor constructor = fieldType.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object obj = constructor.newInstance();
                bind((Map<String, Object>)value, obj);
                field.setAccessible(true);
                field.set(bindingObject, obj);
            }
        }
    }

    public static abstract class BindingSpec<T> {
        abstract Class<T> getType();
        public void bind(Object obj, Field field, T value) throws IllegalAccessException {
            field.setAccessible(true);
            field.set(obj, value);
        }
    }

    private static class StringBinder extends BindingSpec<String> {
        @Override
        Class<String> getType() {
            return String.class;
        }
    }

    private static class IntegerBinder extends BindingSpec<Integer> {
        @Override
        Class<Integer> getType() {
            return Integer.class;
        }
    }

    private static class LongBinder extends BindingSpec<Long> {
        @Override
        Class<Long> getType() {
            return Long.class;
        }
    }

    private static class MapBinder extends BindingSpec<Map> {
        @Override
        Class<Map> getType() {
            return Map.class;
        }
    }

    static void buildMappings(String key, Map<String, Object> map, String val) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            String first = parts[0];
            parts = Arrays.copyOfRange(parts, 1, parts.length);
            String rest = Arrays.asList(parts).stream().collect(Collectors.joining("."));
            if (!map.containsKey(first)) {
                map.put(first, new HashMap<>());
            }
            buildMappings(rest, (Map<String, Object>) map.get(first), val);
        } else {
            map.put(key, val);
        }
    }

    private static class DbConfig {
        private String url;
        private String username;

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }
    }

    private static class MyConfig {
        private String appName;
        private DbConfig db;

        public String getAppName() {
            return appName;
        }

        public DbConfig getDb() {
            return db;
        }
    }

    private static class ServerConfig {
        private int port;
        private Map<String, String> configs;

        public int getPort() {
            return port;
        }

        public Map<String, String> getConfigs() {
            return configs;
        }
    }
}
