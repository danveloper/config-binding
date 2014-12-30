package config

import spock.lang.Specification


import static config.support.ConfigurationSpecSupport.get

class TypedMapBindingSpec extends Specification {

  void "should convert map types according to their generic signature"() {
    setup:
      def props = """\
        |registry.${dbName1}.url=$dbUrl1
        |registry.${dbName1}.username=$dbUsername1
        |registry.${dbName2}.url=$dbUrl2
        |registry.${dbName2}.username=$dbUsername2
      """.stripMargin()

    when:
      def dbRegistryConfig = get(props).get(DbRegistry)

    then:
      2 == dbRegistryConfig.registry.size()
      dbRegistryConfig.registry.keySet().toList() == [dbName1, dbName2]
      dbRegistryConfig.registry[dbName1].url == dbUrl1
      dbRegistryConfig.registry[dbName1].username == dbUsername1
      dbRegistryConfig.registry[dbName2].url == dbUrl2
      dbRegistryConfig.registry[dbName2].username == dbUsername2

    where:
      dbName1 = "test"
      dbUrl1 = "jdbc:mysql://test/test"
      dbUsername1 = "root"
      dbName2 = "prod"
      dbUrl2 = "jdbc:mysql://prod/prod"
      dbUsername2 = "user"
  }

  static class DbConfig {
    String url
    String username
  }

  static class DbRegistry {
    Map<String, DbConfig> registry
  }
}
