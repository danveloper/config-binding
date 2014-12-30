package config

import spock.lang.Specification
import static config.support.ConfigurationSpecSupport.*

class NestedObjectBindingSpec extends Specification {
  void "should map nested config object"() {
    setup:
      def props = """\
        |db.url=$dbUrl
        |db.username=$dbUser
      """.stripMargin()

    when:
      def nestedConfig = config().add(propFile(props)).get(NestedConfig)

    then:
      nestedConfig.db.url == dbUrl
      nestedConfig.db.username == dbUser

    where:
      dbUrl = "jdbc:mysql://local/test"
      dbUser = "sa"
  }

  static class Db {
    String url
    String username
  }

  static class NestedConfig {
    Db db
  }

}
