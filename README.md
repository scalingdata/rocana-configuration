# Rocana Configuration

`rocana-configuration` is a JSON-like configuration format intended to be handled by humans.  Some of the additional functionality includes:
* Comments
* Dictionary keys without quotes
* Rich data types such as floats, doubles, data sizes, and durations
* Java API for marshalling configuration files to types

## Examples

Currently, examples come in the form of unit tests.
* Example complex Java types in [src/test/java/com/rocana/configuration](src/test/java/com/rocana/configuration)
* Tests for reading Configurations into Java types in [TestConfigurationParser](src/test/java/com/rocana/configuration/TestConfigurationParser.java)
* Configurations examples for `TestConfigurationParser` in [src/test/resources/conf](src/test/resources/conf)

## Comparison with JSON

JSON is a great data interchange format, but is often used as a configuration format.  JSON can be tedious to modify by hand because of required quoting for both keys and values.

## Comparison with Java Properties

Java Properties files are the de-facto standard for configuration in Java projects.  However, they lack a few features that would improve editing by hand:
* Nested data structures
* Non-string keys and values
* Arrays
