[![javadoc](https://javadoc.io/badge2/org.creekservice/creek-system-test-extension/javadoc.svg)](https://javadoc.io/doc/org.creekservice/creek-system-test-extension)
# Creek system test extension

Contains base types for implementing extensions to the system tests.

Creek is designed to be extendable. [Extensions can be written][1] to allow services to interact with new types of
external resource e.g. a new database type or an AWS resource like S3.  When extending Creek in this way, the 
implementer of the extension will also likely want to provide an extension to Creek system tests to enable users
of the extension to test their services.

System test extensions allow extension designers to ... [coming soon!](https://github.com/creek-service/creek-system-test/issues/20)

[1]: https://github.com/creek-service/creek-service/tree/main/extension