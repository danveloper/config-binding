package config.support

import config.ConfigurationSpec
import config.DefaultConfiguration

class ConfigurationSpecSupport {
  static ConfigurationSpec get(String props) {
    def config = new DefaultConfiguration()
    def propertiesConfigSource = config.PropertiesConfigurationSource.load(new ByteArrayInputStream(props.bytes))
    config.add(propertiesConfigSource)
    config
  }
}
