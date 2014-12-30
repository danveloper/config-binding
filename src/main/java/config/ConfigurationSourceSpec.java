package config;

import java.util.Map;

public interface ConfigurationSourceSpec {

    Map<String, Object> getBindingMap();
}
