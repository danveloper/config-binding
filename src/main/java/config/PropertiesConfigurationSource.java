package config;

import config.internal.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads configuration from a classpath properties file
 */
public class PropertiesConfigurationSource implements ConfigurationSourceSpec {

    private final Map<String, Object> bindingMap = new LinkedHashMap<>();

    public static PropertiesConfigurationSource load(String propertiesFile) throws BindingException {
        return load(PropertiesConfigurationSource.class.getClassLoader(), propertiesFile);
    }

    public static PropertiesConfigurationSource load(ClassLoader classLoader, String propertiesFile) throws BindingException {
        Properties properties = new Properties();
        try {
            properties.load(classLoader.getResourceAsStream(propertiesFile));
            return new PropertiesConfigurationSource().load(properties);
        } catch (IOException e) {
            throw new BindingException(e);
        }
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
                map.put(first, new HashMap<>());
            }
            return buildMappings(rest, (Map<String, Object>) map.get(first), val);
        } else {
            map.put(key, val);
        }
        return map;
    }

}
