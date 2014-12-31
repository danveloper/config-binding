package config;

import config.internal.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Loads configuration from a classpath properties file
 */
public class PropertiesConfigurationSource implements ConfigurationSourceSpec {
    private static final Pattern ARRAY_NOTATION = Pattern.compile("(.*)\\[(\\d+)\\]$");

    private final Map<String, Object> bindingMap = new LinkedHashMap<>();

    public static PropertiesConfigurationSource load(File propertiesFile) throws IOException {
        return load(new FileInputStream(propertiesFile));
    }

    public static PropertiesConfigurationSource load(String propertiesFile) throws IOException {
        return load(PropertiesConfigurationSource.class.getClassLoader(), propertiesFile);
    }

    public static PropertiesConfigurationSource load(ClassLoader classLoader, String propertiesFile) throws IOException {
        return load(classLoader.getResourceAsStream(propertiesFile));
    }

    public static PropertiesConfigurationSource load(InputStream stream) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        return load0(properties);
    }

    private static PropertiesConfigurationSource load0(Properties properties) {
        return new PropertiesConfigurationSource().load(properties);
    }

    private PropertiesConfigurationSource load(Properties properties) {
        for (Object objKey : properties.keySet()) {
            if (!(objKey instanceof String)) {
                continue;
            }

            String key = (String) objKey;
            buildMappings(key, bindingMap, properties.getProperty(key));
        }
        return this;
    }

    @Override
    public Map<String, Object> getBindingMap() {
        return this.bindingMap;
    }

    @SuppressWarnings("unchecked")
    private static Object buildMappings(String key, Object container, String val) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            String propKey = parts[0];
            parts = Arrays.copyOfRange(parts, 1, parts.length);
            String rest = Arrays.asList(parts).stream().collect(Collectors.joining("."));

            Matcher arrayMatcher = ARRAY_NOTATION.matcher(propKey);
            boolean isArray = arrayMatcher.find();

            if (isArray) {
                propKey = arrayMatcher.group(1);
            }

            Object nextContainer = container;
            if (container instanceof Map && isArray && !((Map) container).containsKey(propKey)) {
                int idx = Integer.parseInt(arrayMatcher.group(2));

                List value = new ArrayList<>();
                padTo(value, idx);

                Map<String, Object> targetMap = new LinkedHashMap<>();
                value.set(idx, targetMap);
                nextContainer = targetMap;

                ((Map) container).put(propKey, value);
            } else if (container instanceof Map && isArray && ((Map) container).containsKey(propKey)) {
                List value = (List)((Map) container).get(propKey);
                int idx = Integer.parseInt(arrayMatcher.group(2));

                padTo(value, idx);
                if (value.get(idx) == null) {
                    value.set(idx, new LinkedHashMap<>());
                }

                nextContainer = value.get(idx);
            } else if (container instanceof Map && !isArray && !((Map) container).containsKey(propKey)) {
                nextContainer = new LinkedHashMap<>();
                ((Map) container).put(propKey, nextContainer);
            } else {
                nextContainer = ((Map)container).get(propKey);
            }

            return buildMappings(rest, nextContainer, val);
        } else {
            Matcher arrayMatcher = ARRAY_NOTATION.matcher(key);
            boolean isArray = arrayMatcher.find();
            if (isArray) {
                key = arrayMatcher.group(1);
            }

            if (container instanceof Map && isArray && !((Map) container).containsKey(key)) {
                int idx = Integer.parseInt(arrayMatcher.group(2));

                List value = new ArrayList<>();
                padTo(value, idx);

                value.set(idx, val);

                ((Map) container).put(key, value);
            } else if (container instanceof Map && isArray && ((Map) container).containsKey(key)) {
                List value = (List)((Map) container).get(key);
                int idx = Integer.parseInt(arrayMatcher.group(2));
                padTo(value, idx);
                value.set(idx, val);
            } else {
                ((Map<String, Object>)container).put(key, val);
            }
        }
        return container;
    }

    private static void padTo(List list, int size) {
        int startingSize = list.size()-1;
        if (size > startingSize) {
            for (int i=startingSize;i<size;i++) {
                list.add(null);
            }
        }
    }

}
