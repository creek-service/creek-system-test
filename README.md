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

## Defining system tests

System tests are defined in YAML files. Read more on [defining system tests](model).

## Running system tests

There is a [gradle plugin][1] for running system tests as part of a Gradle build.

Happy to take contributions for a [Maven plugin](https://github.com/creek-service/creek-system-test/issues/2).

## Extending system tests

Creek is designed to be extendable. See how to [extend system tests](extension).

## Modules

* **[executor](executor):** functionality for executing system tests.
* **[extension](extension):** defines types required to implement extensions to the system tests.
* **[model](model):** system test model

[1]: https://github.com/creek-service/creek-system-test-gradle-plugin