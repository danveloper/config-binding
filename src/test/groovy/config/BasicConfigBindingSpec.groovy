package config

import spock.lang.Specification


import static config.support.ConfigurationSpecSupport.get

class BasicConfigBindingSpec extends Specification {
  void "should bind base object from config source"() {
    setup:
      def props = """\
        |appName=$appName
        |port=$port
      """.stripMargin()

    when:
      def myConfig = get(props).get(MyConfig)

    then:
      myConfig.appName == appName
      myConfig.port == port

    where:
      appName = "ratpack-app"
      port = 8080
  }

  static class MyConfig {
    String appName
    int port
  }
}
