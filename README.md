[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![build](https://github.com/creek-service/creek-system-test/actions/workflows/gradle.yml/badge.svg)](https://github.com/creek-service/creek-system-test/actions/workflows/gradle.yml)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-system-test/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-system-test?branch=main)

# Creek System Test

This repo provides functionality for executing system tests.

Simply put, system tests provide black-box testing of micro-services. The system tests start your service or services,
and any additional 3rd-party services they need e.g. Kafka, in local docker containers, so that it can run through
the system test cases you've defined.

System test cases are written YAML and define any initial seed data; the inputs to feed into the system; and the
expected output or final state of the system.

## Modules

* **[executor](executor):** functionality for executing system tests.

### Gradle commands

* `./gradlew format` will format the code using [Spotless][1].
* `./gradlew static` will run static code analysis, i.e. [Spotbugs][2] and [Checkstyle][3].
* `./gradlew check` will run all checks and tests.
* `./gradlew coverage` will generate a cross-module [Jacoco][4] coverage report.

[1]: https://github.com/diffplug/spotless
[2]: https://spotbugs.github.io/
[3]: https://checkstyle.sourceforge.io/
[4]: https://www.jacoco.org/jacoco/trunk/doc/
