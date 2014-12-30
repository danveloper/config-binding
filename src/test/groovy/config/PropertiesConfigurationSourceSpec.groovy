package config

import spock.lang.Specification

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
      def configSource = PropertiesConfigurationSource.load(new ByteArrayInputStream(props.bytes))

    expect:
      configSource.bindingMap == [appName: appName, port: port.toString(), db: [url: dbUrl, username: dbUser]]
  }
}
