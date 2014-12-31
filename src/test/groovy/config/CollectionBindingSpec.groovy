package config

import spock.lang.Specification


import static config.support.ConfigurationSpecSupport.config
import static config.support.ConfigurationSpecSupport.propFile

class CollectionBindingSpec extends Specification {
  def props1 = """\
    |dbConfigs[0].url=jdbc:mysql://test/test
    |dbConfigs[0].username=foo
  """.stripMargin()

  void "should bind collections properly"() {
    given:
      def myConfig = config().add(propFile(props1)).get(MyConfig)

    expect:
      1 == myConfig.dbConfigs.size()
  }

  static class DbConfig {
    String url
    String username
  }

  static class MyConfig {
    List<DbConfig> dbConfigs
  }
}
