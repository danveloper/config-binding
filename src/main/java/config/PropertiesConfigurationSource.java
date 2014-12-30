package config;

import config.internal.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads configuration from a classpath properties file
 */
public class PropertiesConfigurationSource implements ConfigurationSourceSpec {

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
    private static Map<String, Object> buildMappings(String key, Map<String, Object> map, String val) {
        if (key.contains(".")) {
            String[] parts = key.split("\\.");
            String first = parts[0];
            parts = Arrays.copyOfRange(parts, 1, parts.length);
            String rest = Arrays.asList(parts).stream().collect(Collectors.joining("."));
            if (!map.containsKey(first)) {
                map.put(first, new LinkedHashMap<>());
            }
            return buildMappings(rest, (Map<String, Object>) map.get(first), val);
        } else {
            map.put(key, val);
        }
        return map;
    }

}
