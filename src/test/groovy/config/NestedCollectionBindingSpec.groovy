package config

import spock.lang.Specification


import static config.support.ConfigurationSpecSupport.config
import static config.support.ConfigurationSpecSupport.propFile

class NestedCollectionBindingSpec extends Specification {
  def props1 = """\
    |dbConfigs[0].url=jdbc:mysql://test/test
    |dbConfigs[0].username=foo
    |dbConfigs[0].aliases[0]=test
    |dbConfigs[0].aliases[1]=dev
    |dbConfigs[0].aliases[2]=prod
    |dbConfigs[0].deeperConfigs[0].title=deep1
    |dbConfigs[0].deeperConfigs[1].title=deep2
  """.stripMargin()

  void "should bind collections properly"() {
    given:
      def myConfig = config().add(propFile(props1)).get(MyConfig)

    expect:
      1 == myConfig.dbConfigs.size()
      3 == myConfig.dbConfigs[0].aliases.size()
      2 == myConfig.dbConfigs[0].deeperConfigs.size()
  }

  static class DeeperConfig {
    String title
  }

  static class DbConfig {
    String url
    String username
    List<String> aliases
    Set<DeeperConfig> deeperConfigs
  }

  static class MyConfig {
    List<DbConfig> dbConfigs
  }
}
