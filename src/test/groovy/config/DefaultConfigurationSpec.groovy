package config

import spock.lang.Specification

class DefaultConfigurationSpec extends Specification {

  private static ConfigurationSpec get(String props) {
    def config = new DefaultConfiguration()
    def propertiesConfigSource = PropertiesConfigurationSource.load(new ByteArrayInputStream(props.bytes))
    config.add(propertiesConfigSource)
    config
  }

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

  void "should map to a bunch of different types"() {
    setup:
      def props = """\
        |stringType=$stringType
        |objectIntegerType=$intType
        |primitiveIntegerType=$intType
        |options.a=$aVal
        |options.b=$bVal
      """.stripMargin()

    when:
      def allTypesConfig = get(props).get(AllTypesConfig)

    then:
      allTypesConfig.stringType == stringType
      allTypesConfig.objectIntegerType == intType
      allTypesConfig.primitiveIntegerType == intType
      allTypesConfig.options == [a: aVal, b: bVal]

    where:
      stringType = "foo"
      intType = 1234
      aVal = "a"
      bVal = "b"
  }

  void "should be able to use a custom type converter"() {
    setup:
      def props = """\
        |db.url=jdbc:mysql://local/test
        |db.username=sa
      """.stripMargin()

    when:
      def nestedConfig = get(props).converter(new DbConverter()).get(NestedConfig)

    then:
      nestedConfig.db instanceof ManufactueredDb
  }

  void "should map nested config object"() {
    setup:
      def props = """\
        |db.url=$dbUrl
        |db.username=$dbUser
      """.stripMargin()

    when:
      def nestedConfig = get(props).get(NestedConfig)

    then:
      nestedConfig.db.url == dbUrl
      nestedConfig.db.username == dbUser

    where:
      dbUrl = "jdbc:mysql://local/test"
      dbUser = "sa"
  }

  static class MyConfig {
    String appName
    int port
  }

  static class AllTypesConfig {
    String stringType
    Integer objectIntegerType
    int primitiveIntegerType
    Map<String, String> options
  }

  static class Db {
    String url
    String username
  }

  static class NestedConfig {
    Db db
  }

  static class ManufactueredDb extends Db {}

  static class DbFactory {
    static Db db() {
      new ManufactueredDb()
    }
  }

  static class DbConverter implements TypeConverter<Db> {

    @Override
    boolean handles(Class clazz) {
      Db.isAssignableFrom(clazz)
    }

    @Override
    Db convert(Object value) {
      DbFactory.db()
    }
  }
}
