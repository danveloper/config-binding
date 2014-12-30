package config

import spock.lang.Specification


import static config.support.ConfigurationSpecSupport.get

class CustomTypeConverterSpec extends Specification {
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

  static class Db {
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
