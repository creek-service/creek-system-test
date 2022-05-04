# Creek system test parser

Parser of system test files.

The following code snippet can be used to parse a directory containing a test package:

```java
class ExampleTestParsing {
    TestPackage parse(
            Collection<ModelType<?>> modelExtensions,
            TestPackageLoader.Observer observer,
            Path rootPath
    ) {
        return TestPackageLoaders
                .yamlLoader(modelExtensions, observer)
                .load(rootPath, path -> true).orElseThrow();
    }
}
```