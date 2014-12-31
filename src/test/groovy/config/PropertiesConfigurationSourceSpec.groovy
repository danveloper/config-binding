package config

import spock.lang.Specification


import static config.support.ConfigurationSpecSupport.propFile

class PropertiesConfigurationSourceSpec extends Specification {

  def appName = "ratpack-app"
  def port = 8080
  def dbUrl = "jdbc:mysql://local/test"
  def dbUser = "sa"

  def props = """\
    |appName=$appName
    |port=$port
    |db.url=$dbUrl
    |db.username=$dbUser
  """.stripMargin()

  void "should map properties"() {
    given:
      def configSource = propFile(props)

    expect:
      configSource.bindingMap == [appName: appName, port: port.toString(), db: [url: dbUrl, username: dbUser]]
  }

  void "should map array types to collection"() {
    setup:
      def props = """\
        |dbs[0].name=test
        |dbs[0].url=jdbc:mysql://test/test
        |dbs[1].name=prod
        |dbs[1].url=jdbc:mysql://prod/prod
      """.stripMargin()

    when:
      def configSource = propFile(props)

    then:
      configSource.bindingMap == [dbs: [[name: "test", url: "jdbc:mysql://test/test"],
                                        [name: "prod", url: "jdbc:mysql://prod/prod"]]]
  }

  void "tailing collections should be properly coerced"() {
    setup:
      def props = """\
        |dbs[0]=test
        |dbs[1]=dev
        |dbs[2]=prod
      """.stripMargin()

    when:
      def configSource = propFile(props)

    then:
      configSource.bindingMap == [dbs: ["test", "dev", "prod"]]
  }

  void "out of order collections should be indexed properly"() {
    setup:
      def props = """\
        |dbs[1]=dev
        |dbs[0]=test
        |dbs[2]=prod
      """.stripMargin()

    when:
      def configSource = propFile(props)

    then:
      configSource.bindingMap == [dbs: ["test", "dev", "prod"]]
  }
}
