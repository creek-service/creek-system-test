# Creek system test parser

Parser of system test files.

The following code snippet can be used to parse a single test package from a directory:

```java
class ExampleTestParsing {
    TestPackage parse(
            Collection<ModelType<?>> modelExtensions,
            TestPackageLoader.Observer observer,
            Path rootPath
    ) {
        return TestPackageParsers
                .yaml(modelExtensions, observer)
                .load(rootPath, path -> true)
                .orElseThrow();
    }
}
```

...or a directory structure of test packages can be parsed with:

```java
class ExampleTestParsing {
    List<TestPackage> parse(
            Collection<ModelType<?>> modelExtensions,
            TestPackageLoader.Observer observer,
            Path rootPath
    ) {
        TestPackagesLoader loader = TestPackagesLoader
                .testPackagesLoader(rootPath, TestPackageParsers.yaml(modelExtensions, observer), path -> true);
        
        // The stream must be closed to ensure filesystem resources are released:
        try (Stream<TestPackage> s = loader.stream()){
            return s.collect(Collectors.toList());
        }
    }
}
```
