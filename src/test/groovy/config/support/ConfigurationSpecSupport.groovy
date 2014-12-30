package config.support

import config.ConfigurationSpec
import config.DefaultConfiguration
import config.PropertiesConfigurationSource

class ConfigurationSpecSupport {
  static ConfigurationSpec get(String props) {
    def config = new DefaultConfiguration()
    def propertiesConfigSource = PropertiesConfigurationSource.load(new ByteArrayInputStream(props.bytes))
    config.add(propertiesConfigSource)
    config
  }
}
