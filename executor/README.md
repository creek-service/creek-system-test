# Creek system test runner

Provides functionality for executing system tests.

> ## NOTE
> There is a [Gradle plugin][1] for running system tests as part of a Gradle build that should be used in preference
> to invoking the test executor manually!

This test runner is designed to be run from build plugins, like the [Creek System Test Gradle plugin][1].
However, it can be run directly as a command line tool: 

```shell
    java \
      --module-path <lib-path> \
      --module creek.system.test.executor/org.creek.api.system.test.executor.SystemTestExecutor \
      --test-directory path/to/tests \
      --result-directory path/to/results
```

(Run with `--help` for an up-to-date list of arguments)

...or you can interact programmatically with the main [SystemTestExecutor][2] class.

[1]: https://github.com/creek-service/creek-system-test-gradle-plugin
[2]: src/main/java/org/creek/api/system/test/executor/SystemTestExecutor.java