package config

import spock.lang.Specification

import static config.support.ConfigurationSpecSupport.*
class MergedSourcesBindingSpec extends Specification {
  def props1 = """\
    |dbs.test.url=jdbc:mysql://test/test
    |dbs.test.username=root
    |dbs.prod.url=jdbc:mysql://prod/prod
    |dbs.prod.username=defaultUsername
  """.stripMargin()

  def props2 = """\
    |dbs.prod.username=actualUsername
  """.stripMargin()

  void "should merge config binding maps"() {
    given:
      def registry = config().add(propFile(props1)).add(propFile(props2)).get(DbRegistry)

    expect:
      2 == registry.dbs.size()
      registry.dbs.prod.username == "actualUsername"
  }

  static class DbConfig {
    String url
    String username
  }

  static class DbRegistry {
    Map<String, DbConfig> dbs
  }
}
