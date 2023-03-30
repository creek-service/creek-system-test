[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-system-test/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-system-test?branch=main)
[![build](https://github.com/creek-service/creek-system-test/actions/workflows/build.yml/badge.svg)](https://github.com/creek-service/creek-system-test/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.creekservice/creek-system-test-executor.svg)](https://central.sonatype.dev/search?q=creek-system-test-*)
[![CodeQL](https://github.com/creek-service/creek-system-test/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-system-test/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-system-test/badge)](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-system-test)
[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/6899/badge)](https://bestpractices.coreinfrastructure.org/projects/6899)

# Creek System Test

This repo provides functionality for executing system tests.

See [CreekService.org](https://www.creekservice.org/creek-system-test) for info on Creek Service.

## Modules in this repository

* **[executor](executor)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-system-test-executor)]: functionality for executing system tests.
* **[extension](extension)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-system-test-extension)]: defines types required to implement extensions to the system tests.
* **[model](model)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-system-test-model)]: system test model.
* **[parser](parser)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-system-test-parser)]: code for parsing system tests.
* **[test-util](test-util)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-system-test-test-util)]: contains some utilities to help test system test extensions.

Internal / Non-published:
* **[test-service](test-service):** creates a docker image container a simple microservice used for testing within this repo
* **[test-services](test-services):** creates a jar containing the metadata for [test-service](test-service), i.e. the service descriptor. 
* **[test-service-extension-metadata](test-service-extension-metadata):** the metadata types for a fictitious service extension used internally during testing.
* **[test-system-test-extension](test-system-test-extension):** a system test extension used internally during testing.
