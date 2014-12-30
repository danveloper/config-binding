package config

import spock.lang.Specification

import static config.support.ConfigurationSpecSupport.*

class BasicTypedBindingSpec extends Specification {
  void "should map to a bunch of different types"() {
    setup:
      def props = """\
        |stringType=$stringType
        |objectIntegerType=$intType
        |primitiveIntegerType=$intType
        |objectLongType=$longType
        |primitiveLongType=$longType
        |options.a=$aVal
        |options.b=$bVal
      """.stripMargin()

    when:
      def allTypesConfig = config().add(propFile(props)).get(AllTypesConfig)

    then:
      allTypesConfig.stringType == stringType
      allTypesConfig.objectIntegerType == intType
      allTypesConfig.primitiveIntegerType == intType
      allTypesConfig.objectLongType == longType
      allTypesConfig.primitiveLongType == longType
      allTypesConfig.options == [a: aVal, b: bVal]

    where:
      stringType = "foo"
      intType = 1234
      longType = 1l
      aVal = "a"
      bVal = "b"
  }

  static class AllTypesConfig {
    String stringType
    Integer objectIntegerType
    int primitiveIntegerType
    Long objectLongType
    long primitiveLongType
    Map<String, String> options
  }
}
