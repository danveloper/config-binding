package config.support

import config.ConfigurationSpec
import config.DefaultConfiguration
import config.PropertiesConfigurationSource

class ConfigurationSpecSupport {
  static ConfigurationSpec config() {
    new DefaultConfiguration()
  }

  static PropertiesConfigurationSource propFile(String src) {
    PropertiesConfigurationSource.load(new ByteArrayInputStream(src.bytes))
  }
}
